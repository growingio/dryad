package io.growing.dryad.git2consul.hook

import io.undertow.server.HttpServerExchange

/**
 * Component:
 * Description:
 * Date: 2018-12-24
 *
 * @author AI
 */
trait ConfigurationHook {

  val name: String

  def call(exchange: HttpServerExchange): Unit

}
