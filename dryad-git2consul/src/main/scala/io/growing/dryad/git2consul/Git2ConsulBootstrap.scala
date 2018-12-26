package io.growing.dryad.git2consul

import com.typesafe.config.ConfigFactory
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.router.WebHookRouter
import io.undertow.Undertow
import io.undertow.server.RoutingHandler
import org.jmotor.undertow.logger.AccessLogHandler

/**
 * Component:
 * Description:
 * Date: 2018-12-19
 *
 * @author AI
 */
object Git2ConsulBootstrap extends App {

  val router = new WebHookRouter(Git2ConsulConfig.parse(ConfigFactory.load()))
  val handler = new RoutingHandler()
  router.methods.foreach(method ⇒ handler.add(method, router.route, new AccessLogHandler(router)))
  val undertow = Undertow.builder()
    .addHttpListener(8085, "0.0.0.0")
    .setHandler(handler).build()

  undertow.start()

}
