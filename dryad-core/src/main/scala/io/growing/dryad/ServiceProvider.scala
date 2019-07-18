package io.growing.dryad

import java.net.InetAddress

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.{ Config, ConfigFactory }
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.registry.dto.{ LoadBalancing, Portal, Service, ServiceInstance }
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

  def getService: Service

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
  private[this] lazy val deregisterCriticalServiceAfterFactor = 10

  override def register(): Unit = {
    initService(Seq.empty)
    registry.register(service)
  }

  override def deregister(): Unit = {
    checkState()
    registry.deregister(service)
  }

  override def getService: Service = {
    checkState()
    service
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
    }.groupBy(_.split("\\.").head).keys.map { schemaName ⇒
      val portalConfig = serviceConfig.getConfig(schemaName)
      val port = portalConfig.getInt("port")
      val pattern = patterns.collectFirst {
        case (s, ps) if Schema.withName(schemaName.toLowerCase) == s ⇒ ps.mkString(",")
      }.fold(portalConfig.getStringOpt("pattern").getOrElse("/.*"))(identity)
      val nonCertifications = portalConfig.getStringSeqOpt("non-certifications").map(_.distinct).getOrElse(Seq.empty)
      val id = Hashing.murmur3_128().hashString(s"$name-$group-$address-$port", Charsets.UTF_8).toString
      val schema = Schema.withName(schemaName)
      Portal(id, schema, port, pattern, getCheck(id, name, portalConfig, schema, address, port), nonCertifications)
    }.toSet
    val priority = serviceConfig.getIntOpt("priority").getOrElse(0)
    val loadBalancing = serviceConfig.getStringOpt("load-balancing").map(lb ⇒ LoadBalancing.withName(lb))
    Service(name, address, group, portals, priority, loadBalancing)
  }

  private[dryad] def getCheck(id: String, name: String, conf: Config, schema: Schema, address: String, port: Int): HealthCheck = {
    lazy val deregisterCriticalServiceAfterOpt = conf.getDurationOpt("deregister-critical-service-after")
    conf.getConfigOpt("check") match {
      case None ⇒
        val ttl = 10.seconds
        TTLHealthCheck(ttl, ttl * deregisterCriticalServiceAfterFactor)
      case Some(checkConfig) if schema == Schema.HTTP       ⇒ parseHttpCheck(checkConfig, id, name, address, port, deregisterCriticalServiceAfterOpt)
      case Some(checkConfig) if schema == Schema.WEB_SOCKET ⇒ parseHttpCheck(checkConfig, id, name, address, port, deregisterCriticalServiceAfterOpt)
      case Some(checkConfig) if schema == Schema.GRPC       ⇒ parseGrpcCheck(checkConfig, name, address, port, deregisterCriticalServiceAfterOpt)
      case Some(checkConfig)                                ⇒ parseTtlCheck(checkConfig, deregisterCriticalServiceAfterOpt)
    }
  }

  private[dryad] def parseTtlCheck(config: Config, deregisterCriticalServiceAfterOpt: Option[Duration]): TTLHealthCheck = {
    val ttl = getDuration(config, "ttl")
    TTLHealthCheck(ttl, deregisterCriticalServiceAfterOpt.getOrElse(ttl * deregisterCriticalServiceAfterFactor))
  }

  private[dryad] def parseHttpCheck(config: Config, id: String, name: String, address: String, port: Int,
                                    deregisterCriticalServiceAfterOpt: Option[Duration]): HttpHealthCheck = {
    @volatile lazy val interval = paresInterval(config)
    val checkUrl = config.getStringOpt("url").getOrElse(s"/$id/check")
    val url = if (checkUrl.startsWith("/")) s"http://$address:$port$checkUrl" else checkUrl
    val timeout = config.getDurationOpt("timeout").getOrElse(5.seconds)
    val deregisterCriticalServiceAfter = deregisterCriticalServiceAfterOpt
    HttpHealthCheck(url, interval, timeout, deregisterCriticalServiceAfter.getOrElse(interval * deregisterCriticalServiceAfterFactor))
  }

  private[dryad] def parseGrpcCheck(config: Config, name: String, address: String, port: Int,
                                    deregisterCriticalServiceAfterOpt: Option[Duration]): GrpcHealthCheck = {
    val useTls = config.getBoolean("grpc-use-tls")
    @volatile lazy val interval = paresInterval(config)
    val deregisterCriticalServiceAfter = deregisterCriticalServiceAfterOpt.getOrElse(interval * deregisterCriticalServiceAfterFactor)
    GrpcHealthCheck(s"$address:$port/$name", interval, useTls, deregisterCriticalServiceAfter)
  }

  private[dryad] def paresInterval(config: Config): Duration = {
    config.getDurationOpt("interval").getOrElse(10.seconds)
  }

  private[dryad] def getDuration(config: Config, key: String): Duration = {
    config.getDuration(key).toMillis.milliseconds
  }

}
