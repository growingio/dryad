package io.growing.dryad.registry.impl

import java.net.InetAddress

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.Config
import io.growing.dryad.registry.dto.Service
import io.growing.dryad.registry.{HealthCheck, HttpHealthCheck, ServiceProvider, ServiceRegistry, TTLHealthCheck}
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
    val pattern = serviceConfig.getStringOpt("pattern")
    val patterns = serviceConfig.getStringSeqOpt("patterns")
    val priority = serviceConfig.getIntOpt("priority").getOrElse(0)
    val schema = serviceConfig.getStringOpt("schema").getOrElse("http")
    val address = serviceConfig.getStringOpt("address").getOrElse(InetAddress.getLocalHost.getHostAddress)
    val check = getCheck(serviceConfig, schema, address, port)
    val id = Hashing.md5().hashString(address + s"-$port-$group", Charsets.UTF_8).toString
    val _patterns = patterns.getOrElse(pattern.map(p ⇒ Seq(p)).getOrElse(Seq("/*")))
    Service(id, name, schema, address, port, _patterns, group, check, priority)
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
