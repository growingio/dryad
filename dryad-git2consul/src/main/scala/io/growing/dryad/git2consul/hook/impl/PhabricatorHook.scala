package io.growing.dryad.git2consul.hook.impl

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.hook.ConfigurationHook
import io.growing.dryad.git2consul.hook.dto.PhabricatorRequest
import io.growing.dryad.git2consul.json.Jackson
import io.growing.dryad.git2consul.sync.impl.GitConfigurationSyncer
import io.growing.dryad.git2consul.writer.impl.ConsulConfigurationWriter
import io.undertow.server.HttpServerExchange
import io.undertow.util.StatusCodes
import org.apache.logging.log4j.scala.Logging

import scala.util.Try

/**
 * Component:
 * Description:
 * Date: 2018-12-24
 *
 * @author AI
 */
class PhabricatorHook(config: Git2ConsulConfig) extends ConfigurationHook with Logging {

  private[this] val hmackey = config.underlying.getString("git2consul.hooks.phabricator.hmac-key").getBytes(Charsets.UTF_8)
  private[this] val typeless = Try(config.underlying.getBoolean("git2consul.hooks.phabricator.typeless")).getOrElse(false)
  private[this] val writer = new ConsulConfigurationWriter(config)
  private[this] val syncer = new GitConfigurationSyncer(config, writer)

  override val name: String = "phabricator"

  override def call(exchange: HttpServerExchange): Unit = {
    exchange.startBlocking()
    val body = ByteStreams.toByteArray(exchange.getInputStream)
    val sign = Hashing.hmacSha256(hmackey).hashBytes(body).toString
    val webHookSign = exchange.getRequestHeaders.get("X-Phabricator-Webhook-Signature", 0)
    if (sign == webHookSign) {
      val request = Jackson.mapper.readValue(body, classOf[PhabricatorRequest])
      val typ = request.`object`.`type`
      if (typeless || typ == "CMIT") {
        logger.info(s"phabricator hook trigger sync, type: $typ")
        syncer.sync()
      } else {
        logger.warn(s"phabricator hook not a allowed command, type: $typ")
      }
    } else {
      exchange.setStatusCode(StatusCodes.FORBIDDEN)
    }
    exchange.endExchange()
  }

}
