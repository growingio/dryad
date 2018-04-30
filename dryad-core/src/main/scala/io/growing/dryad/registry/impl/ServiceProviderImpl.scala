package io.growing.dryad.registry.impl

import java.net.InetAddress

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.Config
import io.growing.dryad.registry.dto.{ LoadBalancing, Portal, Service }
import io.growing.dryad.registry.{ GrpcHealthCheck, HealthCheck, HttpHealthCheck, ServiceProvider, ServiceRegistry, TTLHealthCheck }
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
class ServiceProviderImpl(config: Config) extends ServiceProvider {
  private[this] lazy val registry: ServiceRegistry = {
    val registryName = config.getString("dryad.registry")
    Class.forName(registryName).newInstance().asInstanceOf[ServiceRegistry]
  }

  private[impl] lazy val service: Service = {
    val group = config.getString("dryad.group")
    val name = config.getString("dryad.namespace")
    val serviceConfig = config.getConfig("dryad.service")
    val address = serviceConfig.getStringOpt("address").getOrElse(InetAddress.getLocalHost.getHostAddress)
    val portals = serviceConfig.entrySet().asScala.collect {
      case entry if entry.getKey.contains('.') ⇒ entry.getKey
    }.groupBy(_.split("\\.").head).keys.map { schema ⇒
      val portalConfig = serviceConfig.getConfig(schema)
      val port = portalConfig.getInt("port")
      val pattern = portalConfig.getStringOpt("pattern").getOrElse("/*")
      val nonCertifications = portalConfig.getStringSeqOpt("non-certifications").map(_.distinct).getOrElse(Seq.empty)
      val id = Hashing.murmur3_128().hashString(address + s"-$port-$group", Charsets.UTF_8).toString
      Portal(id, schema, port, pattern, getCheck(portalConfig, schema, address, port), nonCertifications)
    }.toSet
    val priority = serviceConfig.getIntOpt("priority").getOrElse(0)
    val loadBalancing = serviceConfig.getStringOpt("load-balancing").map(lb ⇒ LoadBalancing.withName(lb))
    Service(name, address, group, portals, priority, loadBalancing)
  }

  private[this] def getCheck(conf: Config, schema: String, address: String, port: Int): HealthCheck = {
    conf.getConfigOpt("check") match {
      case None ⇒ TTLHealthCheck(10.seconds.toSeconds)
      case Some(checkConfig) ⇒
        val ttl = checkConfig.getLongOpt("ttl").map(ttl ⇒ TTLHealthCheck(ttl))
        lazy val interval = checkConfig.getLongOpt("interval").getOrElse(10.seconds.toSeconds)
        val http = checkConfig.getStringOpt("url").map { url ⇒
          val _url = if (url.startsWith("/")) s"$schema://$address:$port$url" else url
          val timeout = checkConfig.getLongOpt("timeout").getOrElse(5.seconds.toSeconds)
          HttpHealthCheck(_url, interval, timeout)
        }
        val grpc = checkConfig.getBooleanOpt("grpc-use-tls").map { useTls ⇒
          GrpcHealthCheck(s"$address:$port", interval, useTls)
        }
        (ttl orElse http orElse grpc).get
    }
  }

  override def online(): Unit = registry.register(service)

  override def offline(): Unit = registry.deregister(service)

}
