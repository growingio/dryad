package io.growing.dryad.registry

import com.typesafe.config.{Config, ConfigFactory}
import io.growing.dryad.registry.impl.ServiceProviderImpl

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
trait ServiceProvider {

  def online(): Unit

  def offline(): Unit

}

object ServiceProvider {

  def apply(): ServiceProvider = new ServiceProviderImpl(ConfigFactory.load())

  def apply(config: Config): ServiceProvider = new ServiceProviderImpl(config)

}
