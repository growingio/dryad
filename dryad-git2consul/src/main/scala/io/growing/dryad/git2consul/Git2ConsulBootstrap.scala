package io.growing.dryad.git2consul

import java.net.InetAddress

import com.typesafe.config.ConfigFactory
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.router.WebHookRouter
import io.undertow.Undertow
import io.undertow.server.RoutingHandler
import org.apache.logging.log4j.scala.Logging
import org.jmotor.undertow.logger.AccessLogHandler

/**
 * Component:
 * Description:
 * Date: 2018-12-19
 *
 * @author AI
 */
object Git2ConsulBootstrap extends App with Logging {

  val config = Git2ConsulConfig.parse(ConfigFactory.load())
  val router = new WebHookRouter(config)
  val handler = new RoutingHandler()
  router.methods.foreach(method ⇒ handler.add(method, router.route, new AccessLogHandler(router)))
  val host = config.server.host.getOrElse(InetAddress.getLocalHost.getHostAddress)
  val undertow = Undertow.builder()
    .addHttpListener(config.server.port, host)
    .setHandler(handler).build()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = undertow.stop()
  })

  undertow.start()

  logger.info(s"server listening on $host:${config.server.port}")

}
