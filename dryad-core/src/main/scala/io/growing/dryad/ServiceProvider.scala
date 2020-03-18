package io.growing.dryad

import java.net.InetAddress
import java.util.Map.Entry

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.{ Config, ConfigFactory, ConfigValue }
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.registry.dto.Schema.Schema
import io.growing.dryad.registry.dto.{ LoadBalancing, Schema, Service, Server, ServiceMeta }
import io.growing.dryad.registry.{ GrpcHealthCheck, HealthCheck, HttpHealthCheck, ServiceRegistry, TTLHealthCheck }
import io.growing.dryad.utils.ConfigUtils._

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

  def getServices: Set[Service]

  def setPatterns(schema: Schema, patterns: String*): Unit

  def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit

  def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[Server]

}

object ServiceProvider {

  def apply(): ServiceProvider = new ServiceProviderImpl(ConfigFactory.load())

  def apply(config: Config): ServiceProvider = new ServiceProviderImpl(config)

}

class ServiceProviderImpl(config: Config) extends ServiceProvider {
  @volatile private[this] lazy val groupConfigPath = "dryad.group"
  @volatile private[this] lazy val registry: ServiceRegistry = {
    val registryName = config.getString("dryad.registry")
    Class.forName(registryName).newInstance().asInstanceOf[ServiceRegistry]
  }
  @volatile private[this] lazy val services = new java.util.ArrayList[Service](parseServices().asJava)
  @volatile private[this] lazy val deregisterCriticalServiceAfterFactor = 10

  override def register(): Unit = {
    services.asScala.foreach(registry.register)
  }

  override def deregister(): Unit = {
    services.asScala.foreach(registry.deregister)
  }

  override def getServices: Set[Service] = {
    services.asScala.toSet
  }

  override def setPatterns(schema: Schema, patterns: String*): Unit = {
    if (patterns.nonEmpty) {
      this.synchronized {
        val newServices = services.asScala.map {
          case service: Service if service.schema == schema ⇒ service.withPatterns(patterns)
          case service: Service                             ⇒ service
        }
        services.clear()
        services.addAll(newServices.asJava)
      }
    }
  }

  override def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit = {
    val group = config.getString(groupConfigPath)
    registry.subscribe(Seq("_global_", group), schema, serviceName, listener)
  }

  override def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[Server] = {
    val group = config.getString(groupConfigPath)
    registry.getInstances(Seq("_global_", group), schema, serviceName, listener)
  }

  private[dryad] def parseServices(): Set[Service] = {
    val group = config.getString(groupConfigPath)
    val name = config.getString("dryad.namespace")
    val rootConfig = config.getConfig("dryad.service")
    val priority = rootConfig.getIntOpt("priority").getOrElse(0)
    val address = rootConfig.getStringOpt("address").getOrElse(InetAddress.getLocalHost.getHostAddress)
    rootConfig.entrySet().asScala.collect {
      case entry: Entry[String, ConfigValue] if entry.getKey.contains('.') ⇒ entry.getKey
    }.groupBy(_.split("\\.").head).keys.map { schemaName ⇒
      val schema = Schema.withName(schemaName)
      val serviceConfig = rootConfig.getConfig(schemaName)
      val port = serviceConfig.getInt("port")
      val pattern = serviceConfig.getStringOpt("pattern").getOrElse("/.*")
      val nonCertifications = serviceConfig.getStringSeqOpt("non-certifications").map(_.distinct).getOrElse(Seq.empty)
      val id = Hashing.murmur3_128().hashString(Seq(name, group, address, port).mkString("-"), Charsets.UTF_8).toString
      val loadBalancing = serviceConfig.getStringOpt("load-balancing").map(lb ⇒ LoadBalancing.withName(lb))
      val meta = ServiceMeta(pattern, nonCertifications, loadBalancing)
      Service(id, name, schema, address, port, group, priority, meta, getCheck(id, name, serviceConfig, schema, address, port))
    }.toSet
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
