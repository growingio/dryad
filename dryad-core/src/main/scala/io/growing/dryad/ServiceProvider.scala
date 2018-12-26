package io.growing.dryad

import java.net.InetAddress

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.{ Config, ConfigFactory }
import io.growing.dryad.registry.dto.{ LoadBalancing, Portal, Service, ServiceInstance }
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.registry.{ GrpcHealthCheck, HealthCheck, HttpHealthCheck, ServiceRegistry, TTLHealthCheck }
import io.growing.dryad.util.ConfigUtils._

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
trait ServiceProvider {

  def register(): Unit

  def deregister(): Unit

  def register(patterns: (Schema, Seq[String])*): Unit

  def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit

  def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[ServiceInstance]

}

object ServiceProvider {

  def apply(): ServiceProvider = new ServiceProviderImpl(ConfigFactory.load())

  def apply(config: Config): ServiceProvider = new ServiceProviderImpl(config)

}

class ServiceProviderImpl(config: Config) extends ServiceProvider {
  @volatile private[this] lazy val groupConfigPath = "dryad.group"
  private[this] var service: Service = _
  @volatile private[this] lazy val registry: ServiceRegistry = {
    val registryName = config.getString("dryad.registry")
    Class.forName(registryName).newInstance().asInstanceOf[ServiceRegistry]
  }

  override def register(): Unit = {
    initService(Seq.empty)
    registry.register(service)
  }

  override def deregister(): Unit = {
    checkState()
    registry.deregister(service)
  }

  override def register(patterns: (Schema, Seq[String])*): Unit = {
    initService(patterns)
    registry.register(service)
  }

  override def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit = {
    val group = config.getString(groupConfigPath)
    registry.subscribe(Seq("_global_", group), schema, serviceName, listener)
  }

  override def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[ServiceInstance] = {
    val group = config.getString(groupConfigPath)
    registry.getInstances(Seq("_global_", group), schema, serviceName, listener)
  }

  def getService: Service = {
    checkState()
    service
  }

  private[this] def checkState(): Unit = {
    if (Option(service).isEmpty) {
      throw new IllegalStateException("service not init")
    }
  }

  private[dryad] def initService(patterns: Seq[(Schema, Seq[String])]): Unit = {
    if (Option(service).isEmpty) {
      this.synchronized {
        if (Option(service).isEmpty) {
          service = buildService(patterns)
        }
      }
    }
  }

  private[dryad] def buildService(patterns: Seq[(Schema, Seq[String])]): Service = {
    val group = config.getString(groupConfigPath)
    val name = config.getString("dryad.namespace")
    val serviceConfig = config.getConfig("dryad.service")
    val address = serviceConfig.getStringOpt("address").getOrElse(InetAddress.getLocalHost.getHostAddress)
    val portals = serviceConfig.entrySet().asScala.collect {
      case entry if entry.getKey.contains('.') ⇒ entry.getKey
    }.groupBy(_.split("\\.").head).keys.map { schema ⇒
      val portalConfig = serviceConfig.getConfig(schema)
      val port = portalConfig.getInt("port")
      val pattern = patterns.collectFirst {
        case (s, ps) if Schema.withName(schema.toLowerCase) == s ⇒ ps.mkString(",")
      }.fold(portalConfig.getStringOpt("pattern").getOrElse("/.*"))(identity)
      val nonCertifications = portalConfig.getStringSeqOpt("non-certifications").map(_.distinct).getOrElse(Seq.empty)
      val id = Hashing.murmur3_128().hashString(address + s"-$port-$group", Charsets.UTF_8).toString
      Portal(id, Schema.withName(schema), port, pattern, getCheck(name, portalConfig, schema, address, port), nonCertifications)
    }.toSet
    val priority = serviceConfig.getIntOpt("priority").getOrElse(0)
    val loadBalancing = serviceConfig.getStringOpt("load-balancing").map(lb ⇒ LoadBalancing.withName(lb))
    Service(name, address, group, portals, priority, loadBalancing)
  }

  private[dryad] def getCheck(name: String, conf: Config, schema: String, address: String, port: Int): HealthCheck = {
    val factor = 10
    conf.getConfigOpt("check") match {
      case None ⇒
        val ttl = 10.seconds
        TTLHealthCheck(ttl, ttl.*(factor))
      case Some(checkConfig) ⇒
        val deregisterCriticalServiceAfterOpt = conf.getDurationOpt("deregister-critical-service-after")
        val ttl = checkConfig.getDurationOpt("ttl").map { ttl ⇒
          val deregisterCriticalServiceAfter = deregisterCriticalServiceAfterOpt.getOrElse(ttl.*(factor))
          TTLHealthCheck(ttl, deregisterCriticalServiceAfter)
        }
        @volatile lazy val interval = checkConfig.getDurationOpt("interval").getOrElse(10.seconds)
        val http = checkConfig.getStringOpt("url").map { url ⇒
          val _url = if (url.startsWith("/")) s"$schema://$address:$port$url" else url
          val timeout = checkConfig.getDurationOpt("timeout").getOrElse(5.seconds)
          val deregisterCriticalServiceAfter = deregisterCriticalServiceAfterOpt.getOrElse(interval.*(factor))
          HttpHealthCheck(_url, interval, timeout, deregisterCriticalServiceAfter)
        }
        val grpc = checkConfig.getBooleanOpt("grpc-use-tls").map { useTls ⇒
          val deregisterCriticalServiceAfter = deregisterCriticalServiceAfterOpt.getOrElse(interval.*(factor))
          GrpcHealthCheck(s"$address:$port/$name", interval, useTls, deregisterCriticalServiceAfter)
        }
        (ttl orElse http orElse grpc).get
    }
  }

}
