package io.growing.dryad.cluster.impl

import java.net.InetSocketAddress
import java.net.Socket
import java.util.Objects
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.Sets
import com.typesafe.scalalogging.LazyLogging
import io.growing.dryad.ServiceProvider
import io.growing.dryad.cluster.Cluster
import io.growing.dryad.cluster.rule.LoadBalanceRule
import io.growing.dryad.cluster.rule.RoundRobin
import io.growing.dryad.concurrent.ShutdownEnabledTimer
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.registry.dto.Schema.Schema
import io.growing.dryad.registry.dto.Server

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal

/**
 *
 * @author AI
 *         2020/2/8
 */
class ClusterImpl(provider: ServiceProvider, checkIntervalOfSeconds: Int = 0) extends Cluster with LazyLogging {
  private[this] var clusterTimer: ShutdownEnabledTimer = _
  private[this] lazy val timerCreated = new AtomicBoolean(false)
  private[this] val clusters = Caffeine.newBuilder().build[String, ServerList]()
  private[this] lazy val unreachableServers = new java.util.HashSet[Server]()
  private[this] val loadBalanceRules = Caffeine.newBuilder().build[String, LoadBalanceRule]()
  private[this] lazy val unreachableServerLock = new ReentrantReadWriteLock()

  if (checkIntervalOfSeconds > 0) {
    this.clusterTimer = setupTimer(checkIntervalOfSeconds, checkAll = true)
    timerCreated.set(true)
  }

  override def makeServerDown(server: Server): Unit = {
    val writeLock = unreachableServerLock.writeLock()
    writeLock.lock()
    unreachableServers.add(server)
    writeLock.unlock()
    if (timerCreated.compareAndSet(false, true)) {
      lazy val defaultInterval = 5
      this.clusterTimer = setupTimer(if (checkIntervalOfSeconds > 0) checkIntervalOfSeconds else defaultInterval, checkAll = false)
    }
  }

  override def roundRobin(schema: Schema, serverName: String): Server = {
    val clusterName = getClusterName(schema, serverName)
    lazy val loader = new java.util.function.Function[String, ServerList]() {
      override def apply(roundRobinName: String): ServerList = {
        val sl = new ServerList()
        val servers = provider.getInstances(schema, serverName, Option(new ClusterServiceInstanceListener(sl)))
        sl.setAllServers(servers)
      }
    }
    val serverList = clusters.get(clusterName, loader)
    val reachableServers = getReachableServers(serverList)
    if (reachableServers.isEmpty) {
      throw new IllegalStateException(s"Cluster [$clusterName] cannot got reachable servers")
    }
    val loadBalanceRule = loadBalanceRules.get(clusterName, new java.util.function.Function[String, LoadBalanceRule]() {
      override def apply(t: String): LoadBalanceRule = new RoundRobin()
    })
    loadBalanceRule.chooseServer(reachableServers, clusterName)
  }

  private[cluster] def setupTimer(interval: Int, checkAll: Boolean): ShutdownEnabledTimer = {
    val timer = new ShutdownEnabledTimer("dryad-cluster-lb-timer", true)
    timer.schedule(new TimerTask {
      override def run(): Unit = {
        val servers = if (checkAll) {
          clusters.asMap().values().asScala.flatMap(_.getAllServers)
        } else {
          val readLock = unreachableServerLock.readLock()
          readLock.lock()
          val unreachableServerSet = Sets.newHashSet(unreachableServers)
          readLock.unlock()
          unreachableServerSet.asScala
        }
        val availableServers = Sets.newHashSet[Server]()
        val unavailableServers = Sets.newHashSet[Server]()
        if (servers.nonEmpty) {
          servers.foreach { server ⇒
            if (isAvailable(server)) availableServers.add(server) else unavailableServers.add(server)
          }
          val writeLock = unreachableServerLock.writeLock()
          writeLock.lock()
          availableServers.asScala.foreach(unreachableServers.remove)
          unavailableServers.asScala.foreach(unreachableServers.add)
          writeLock.unlock()
        }
      }
    }, 0, interval.seconds.toMillis)
    timer
  }

  private[cluster] def getReachableServers(sl: ServerList): Seq[Server] = {
    val readLock = unreachableServerLock.readLock()
    readLock.lock()
    val unreachableServerSet = Sets.newHashSet(unreachableServers)
    readLock.unlock()
    if (Objects.isNull(unreachableServerSet) || unreachableServerSet.isEmpty) {
      sl.getAllServers
    } else {
      sl.getAllServers.filterNot(unreachableServerSet.contains)
    }
  }

  private[cluster] def isAvailable(server: Server): Boolean = {
    val socket = new Socket()
    try {
      val timeout = TimeUnit.SECONDS.toMillis(1).toInt
      socket.setSoTimeout(timeout)
      socket.connect(new InetSocketAddress(server.address, server.port), timeout)
      true
    } catch {
      case NonFatal(t) ⇒
        logger.warn(s"Connect fail, node: $server, message: ${t.getLocalizedMessage}", t)
        false
    } finally {
      if (Objects.nonNull(socket)) {
        Try(socket.close())
      }
    }
  }

  private[cluster] def getClusterName(schema: Schema, serverName: String) = s"$serverName-$schema"

  private[cluster] def getClusterName(server: Server): String = getClusterName(server.schema, server.name)

  private[cluster] class ClusterServiceInstanceListener(sl: ServerList) extends ServiceInstanceListener {
    override def onChange(servers: Seq[Server]): Unit = {
      sl.setAllServers(servers)
    }
  }

}
