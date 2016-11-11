package io.growing.dryad.client

import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
object ConsulClient {
  private[this] val config: Config = ConfigFactory.load()
  private[this] val port: Int = config.getInt("dryad.consul.port")
  private[this] val host: String = config.getString("dryad.consul.host")
  private[this] lazy val client = {
    Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port))
      .withConnectTimeoutMillis(1000)
      .build()
  }

  lazy val kvClient = client.keyValueClient()

  lazy val agentClient = client.agentClient()

  lazy val catalogClient = client.catalogClient()

  def path(namespace: String, group: String, name: String): String = {
    Array(namespace, group, name).filter(_.trim.nonEmpty).mkString("/")
  }

}
