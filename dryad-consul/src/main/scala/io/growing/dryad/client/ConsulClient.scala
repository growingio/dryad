package io.growing.dryad.client

import java.util.concurrent.Callable

import com.google.common.cache.CacheBuilder
import com.google.common.net.HostAndPort
import com.orbitz.consul.{Consul, KeyValueClient}
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
  private[this] val clientCache = CacheBuilder.newBuilder().build[String, KeyValueClient]()
  private[this] val port: Int = config.getInt("dryad.consul.port")
  private[this] val host: String = config.getString("dryad.consul.host")

  def client(namespace: String, group: String): KeyValueClient = {
    val key = namespace + "." + group
    clientCache.get(key, new Callable[KeyValueClient]() {
      override def call(): KeyValueClient = {
        Consul.builder()
          .withHostAndPort(HostAndPort.fromParts(host, port))
          .withConnectTimeoutMillis(1000)
          .build().keyValueClient()
      }
    })
  }

  def path(namespace: String, group: String, name: String): String = {
    Array(namespace, group, name).filter(_.trim.nonEmpty).mkString("/")
  }
}
