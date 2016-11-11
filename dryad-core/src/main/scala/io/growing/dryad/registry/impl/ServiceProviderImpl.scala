package io.growing.dryad.registry.impl

import java.net.InetAddress

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.typesafe.config.Config
import io.growing.dryad.registry.dto.Service
import io.growing.dryad.registry.{ServiceProvider, ServiceRegistry}
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
    val serviceConfig = config.getConfig("dryad.service")
    val local = InetAddress.getLocalHost.getHostAddress
    val port = serviceConfig.getInt("port")
    val group = config.getString("dryad.group")
    val ttl = serviceConfig.getLongOpt("ttl").getOrElse(10.seconds.toSeconds)
    val pattern = serviceConfig.getStringOpt("pattern").getOrElse("/*")
    val schema = serviceConfig.getStringOpt("schema").getOrElse("http")
    val name = config.getString("dryad.namespace")
    val id = Hashing.md5().hashString(local.replace(".", "-") + s"-$port-$group", Charsets.UTF_8).toString
    Service(id, name, schema, local, port, pattern, group, ttl)
  }

  override def online(): Unit = registry.register(service)

  override def offline(): Unit = registry.deregister(service.id)

}
