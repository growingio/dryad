package io.growing.dryad.registry

import com.typesafe.config.{Config, ConfigFactory}
import io.growing.dryad.registry.impl.ServletImpl

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
trait Servlet {

  def online(): Unit

  def offline(): Unit

}

object Servlet {

  def apply(): Servlet = new ServletImpl(ConfigFactory.load())

  def apply(config: Config): Servlet = new ServletImpl(config)

}
