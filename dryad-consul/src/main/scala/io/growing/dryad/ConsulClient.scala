package io.growing.dryad

import java.util.concurrent.Callable

import com.google.common.cache.CacheBuilder
import com.google.common.net.HostAndPort
import com.orbitz.consul.{Consul, KeyValueClient}

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
object ConsulClient {
  private[this] val clientCache = CacheBuilder.newBuilder().build[Environment, KeyValueClient]()

  def client(environment: Environment): KeyValueClient = {
    clientCache.get(environment, new Callable[KeyValueClient]() {
      override def call(): KeyValueClient = {
        Consul.builder()
          .withHostAndPort(HostAndPort.fromParts(environment.host, environment.port))
          .build().keyValueClient()
      }
    })
  }

  def path(environment: Environment, name: String): String = {
    Array(environment.namespace, environment.group, name).filter(_.trim.nonEmpty).mkString("/")
  }
}
