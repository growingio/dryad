package io.growing.dryad.git2consul.writer.impl

import java.nio.file.Path

import com.google.common.net.HostAndPort
import com.orbitz.consul.option.ImmutablePutOptions
import com.orbitz.consul.{ Consul, KeyValueClient }
import io.growing.dryad.git2consul.concurrent.ExecutionContext.Implicits.IO
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.writer.ConfigurationWriter

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

/**
 * Component:
 * Description:
 * Date: 2018-12-24
 *
 * @author AI
 */
class ConsulConfigurationWriter(config: Git2ConsulConfig) extends ConfigurationWriter {

  @volatile private[this] lazy val client: KeyValueClient = {
    val connectTimeout = config.consul.connectTimeout.getOrElse(1.seconds.toMillis)
    import config.consul._
    val builder = Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port))
      .withConnectTimeoutMillis(connectTimeout)
    username.foreach { value ⇒
      builder.withBasicAuth(value, password.getOrElse(""))
    }
    builder.build().keyValueClient()
  }

  override def write(file: Path, name: String): Future[Boolean] = {
    Future {
      val valueOpt = client.getValue(name)
      val version = if (valueOpt.isPresent) {
        valueOpt.get().getModifyIndex
      } else {
        0
      }
      val value = Source.fromFile(file.toFile, "utf-8").mkString
      client.putValue(name, value, 0L, ImmutablePutOptions.builder().cas(version).build())
    }
  }
}
