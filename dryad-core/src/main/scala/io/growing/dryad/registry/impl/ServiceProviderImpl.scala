package io.growing.dryad.registry.impl

import java.net.InetAddress

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.Config
import io.growing.dryad.registry.dto.Service
import io.growing.dryad.registry.{ HealthCheck, HttpHealthCheck, ServiceProvider, ServiceRegistry, TTLHealthCheck }
import io.growing.dryad.util.ConfigUtils._

import scala.concurrent.duration._

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
class ServiceProviderImpl(config: Config) extends ServiceProvider {
  private[this] val registry: ServiceRegistry = {
    val registryName = config.getString("dryad.registry")
    Class.forName(registryName).newInstance().asInstanceOf[ServiceRegistry]
  }

  private[this] val service: Service = {
    val group = config.getString("dryad.group")
    val name = config.getString("dryad.namespace")
    val serviceConfig = config.getConfig("dryad.service")
    val port = serviceConfig.getInt("port")
    val rpcPort = serviceConfig.getIntOpt("rpc.port")
    val priority = serviceConfig.getIntOpt("priority").getOrElse(0)
    val pattern = serviceConfig.getStringOpt("pattern").getOrElse("/*")
    val schema = serviceConfig.getStringOpt("schema").getOrElse("http")
    val nonCertifications = serviceConfig.getStringSeqOpt("non-certifications").map(_.distinct).getOrElse(Seq.empty)
    val address = serviceConfig.getStringOpt("address").getOrElse(InetAddress.getLocalHost.getHostAddress)
    val id = Hashing.murmur3_128().hashString(address + s"-$port-$group", Charsets.UTF_8).toString
    val check = getCheck(serviceConfig, schema, address, port)
    Service(id, name, schema, address, port, pattern, group, check, priority, nonCertifications, rpcPort)
  }

  private[this] def getCheck(conf: Config, schema: String, address: String, port: Int): HealthCheck = {
    conf.getConfigOpt("check") match {
      case None ⇒ TTLHealthCheck(10.seconds.toSeconds)
      case Some(checkConfig) ⇒
        val ttl = checkConfig.getLongOpt("ttl").map(ttl ⇒ TTLHealthCheck(ttl))
        val http = checkConfig.getStringOpt("url").map { url ⇒
          val _url = if (url.startsWith("/")) s"$schema://$address:$port$url" else url
          val interval = checkConfig.getLongOpt("interval").getOrElse(10.seconds.toSeconds)
          val timeout = checkConfig.getLongOpt("timeout").getOrElse(5.seconds.toSeconds)
          HttpHealthCheck(_url, interval, timeout)
        }
        (ttl orElse http).get
    }
  }

  override def online(): Unit = registry.register(service)

  override def offline(): Unit = registry.deregister(service.id)

}
