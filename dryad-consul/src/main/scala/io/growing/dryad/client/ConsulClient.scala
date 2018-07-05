package io.growing.dryad.client

import com.google.common.net.HostAndPort
import com.orbitz.consul.{ AgentClient, CatalogClient, Consul, KeyValueClient }
import com.typesafe.config.{ Config, ConfigFactory }

import scala.util.{ Failure, Success, Try }

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
object ConsulClient {
  private[this] val defaultConnectTimeout: Int = 1000
  private[this] val config: Config = ConfigFactory.load()
  private[this] val port: Int = config.getInt("dryad.consul.port")
  private[this] val host: String = config.getString("dryad.consul.host")
  private[this] val connectTimeout = Try(config.getInt("dryad.consul.connectTimeout")) match {
    case Success(timeout) ⇒ timeout
    case Failure(_)       ⇒ defaultConnectTimeout
  }
  @volatile private[this] lazy val client = {
    Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port))
      .withConnectTimeoutMillis(connectTimeout)
      .build()
  }

  @volatile lazy val kvClient: KeyValueClient = client.keyValueClient()

  @volatile lazy val agentClient: AgentClient = client.agentClient()

  @volatile lazy val catalogClient: CatalogClient = client.catalogClient()

}
