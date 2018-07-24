package io.growing.dryad.consul.registry

import java.math.BigInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{ Executors, TimeUnit }
import java.util.{ List ⇒ JList }

import com.google.common.cache.CacheBuilder
import com.google.common.collect.Lists
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.{ AbstractScheduledService, ServiceManager }
import com.orbitz.consul.async.ConsulResponseCallback
import com.orbitz.consul.model.ConsulResponse
import com.orbitz.consul.model.agent.Registration.RegCheck
import com.orbitz.consul.model.agent.{ ImmutableRegCheck, ImmutableRegistration }
import com.orbitz.consul.model.health.ServiceHealth
import com.orbitz.consul.option.QueryOptions
import com.typesafe.scalalogging.LazyLogging
import io.growing.dryad.consul.client.ConsulClient
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.registry.dto.{ ServiceInstance, Portal, Service }
import io.growing.dryad.registry.{ GrpcHealthCheck, HttpHealthCheck, ServiceRegistry, TTLHealthCheck }

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
class ConsulServiceRegistry extends ServiceRegistry with LazyLogging {
  private[this] val BLOCK_QUERY_MINS = 5
  private[this] val ttlPortals: JList[Portal] = Lists.newCopyOnWriteArrayList()
  private[this] val watchers = CacheBuilder.newBuilder().build[String, Watcher]()
  private val ttlCheckService: AbstractScheduledService = new AbstractScheduledService {
    @volatile private lazy val executorService = executor()

    override def runOneIteration(): Unit = {
      ttlPortals.asScala.foreach { portal ⇒
        executorService.execute(() ⇒ ConsulClient.agentClient.pass(portal.id, s"pass in ${System.currentTimeMillis()}"))
      }
    }

    override def scheduler(): Scheduler = Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS)

    override def shutDown(): Unit = {
      val fixedThreadPool = Executors.newFixedThreadPool(ttlPortals.size())
      ttlPortals.asScala.foreach { portal ⇒
        fixedThreadPool.execute(() ⇒ ConsulClient.agentClient.fail(portal.id, s"system shutdown in ${System.currentTimeMillis()}"))
      }
      fixedThreadPool.shutdown()
      fixedThreadPool.awaitTermination(1, TimeUnit.MINUTES)
    }
  }
  private[this] val ttlCheckScheduler: ServiceManager = new ServiceManager(Seq(ttlCheckService).asJava).startAsync()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      ttlCheckScheduler.stopAsync()
      ttlCheckScheduler.awaitStopped()
    }
  })

  override def register(service: Service): Unit = {
    service.portals.foreach { portal ⇒
      val metas = Map(
        "priority" -> String.valueOf(service.priority),
        "group" -> service.group,
        "schema" -> portal.schema.toString,
        "pattern" -> portal.pattern)
      val optionalEntities = Seq(
        portal.nonCertifications match {
          case Nil    ⇒ None
          case values ⇒ Option("non_certifications" -> values.mkString(","))
        },
        service.loadBalancing.map(lb ⇒ "load_balancing" -> lb.toString)).collect {
          case Some(entity) ⇒ entity
        }.toMap
      val name = getServiceName(portal.schema, service.name)
      val check = buildCheck(portal)
      val registration = ImmutableRegistration.builder()
        .id(portal.id)
        .name(name)
        .address(service.address)
        .port(portal.port)
        .enableTagOverride(true)
        .tags(Seq("microservice", portal.schema.toString, service.group).asJava)
        .check(check).meta((metas ++ optionalEntities).asJava).build()
      ConsulClient.agentClient.register(registration)
      if (portal.check.isInstanceOf[TTLHealthCheck]) {
        ttlPortals.add(portal)
      }
    }
  }

  override def deregister(service: Service): Unit = {
    service.portals.foreach { portal ⇒
      ttlPortals.asScala.find(_.id == portal.id).foreach { s ⇒
        ttlPortals.remove(s)
        ConsulClient.agentClient.fail(portal.id)
      }
      ConsulClient.agentClient.deregister(portal.id)
    }
  }

  override def subscribe(groups: Seq[String], schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit = {
    val name = getServiceName(schema, serviceName)
    watchers.get(name, () ⇒ new Watcher(groups, schema, name, listener))
  }

  override def getInstances(groups: Seq[String], schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[ServiceInstance] = {
    val name = getServiceName(schema, serviceName)
    val response = ConsulClient.healthClient.getHealthyServiceInstances(name)
    listener.foreach(l ⇒ watchers.get(name, () ⇒ new Watcher(groups, schema, name, l)))
    filterInstances(groups, schema, response.getResponse)
  }

  private[registry] def buildCheck(portal: Portal): RegCheck = {
    val formatSeconds = (n: Duration) ⇒ s"${n.toSeconds}s"
    val builder = portal.check match {
      case ttlCheck: TTLHealthCheck ⇒
        ImmutableRegCheck.builder().ttl(formatSeconds(ttlCheck.ttl))
      case httpCheck: HttpHealthCheck ⇒
        ImmutableRegCheck.builder.http(httpCheck.url)
          .interval(formatSeconds(httpCheck.interval)).timeout(formatSeconds(httpCheck.timeout))
      case grpcCheck: GrpcHealthCheck ⇒
        ImmutableRegCheck.builder.grpc(grpcCheck.grpc).grpcUseTls(grpcCheck.useTls)
          .tlsSkipVerify(!grpcCheck.useTls)
          .interval(formatSeconds(grpcCheck.interval))
      case c ⇒ throw new UnsupportedOperationException(s"Unsupported check: ${c.getClass.getName}")
    }
    builder.deregisterCriticalServiceAfter(formatSeconds(portal.check.deregisterCriticalServiceAfter)).build()
  }

  private[this] def getServiceName(schema: Schema, name: String): String = {
    schema match {
      case Schema.HTTP ⇒ name
      case _           ⇒ s"$name-$schema"
    }
  }

  private def filterInstances(groups: Seq[String], schema: Schema, instances: JList[ServiceHealth]): Seq[ServiceInstance] = {
    instances.asScala.collect {
      case health: ServiceHealth if health.getService.getMeta.asScala.exists {
        case ("group", g) ⇒ groups.contains(g)
        case _            ⇒ false
      } ⇒
        val service = health.getService
        ServiceInstance(service.getService, schema, service.getAddress, service.getPort)
    }
  }

  private class Watcher(groups: Seq[String], schema: Schema, serviceName: String, listener: ServiceInstanceListener) {
    private[this] val callback: ConsulResponseCallback[JList[ServiceHealth]] = new ConsulResponseCallback[JList[ServiceHealth]] {
      private[this] val index = new AtomicReference[BigInteger]

      override def onComplete(response: ConsulResponse[JList[ServiceHealth]]): Unit = {
        index.set(response.getIndex)
        watch()
        listener.onChange(filterInstances(groups, schema, response.getResponse))
      }

      override def onFailure(throwable: Throwable): Unit = watch()

      private[this] def watch(): Unit = {
        ConsulClient.healthClient.getHealthyServiceInstances(
          serviceName,
          QueryOptions.blockMinutes(BLOCK_QUERY_MINS, index.get()).build(), callback)
      }

    }

    ConsulClient.healthClient.getHealthyServiceInstances(
      serviceName,
      QueryOptions.blockMinutes(BLOCK_QUERY_MINS, new BigInteger("0")).build(), callback)
  }

}
