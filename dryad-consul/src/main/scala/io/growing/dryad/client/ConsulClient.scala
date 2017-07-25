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
  private[this] lazy val client = {
    Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port))
      .withConnectTimeoutMillis(connectTimeout)
      .build()
  }

  lazy val kvClient: KeyValueClient = client.keyValueClient()

  lazy val agentClient: AgentClient = client.agentClient()

  lazy val catalogClient: CatalogClient = client.catalogClient()

  def path(name: String, namespace: String, group: Option[String] = None): String = {
    val paths = group.fold(Seq(namespace, name))(_group ⇒ Seq(namespace, _group, name))
    paths.filterNot(_.trim.isEmpty).mkString("/")
  }

  def path(name: String, namespace: String, group: String): String = path(name, namespace, Option(group))

}
