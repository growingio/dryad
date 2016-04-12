package io.growing.dryad

import com.typesafe.config.{Config, ConfigFactory}
import io.growing.dryad.internal.impl.{ConfigGarageImpl, ConfigServiceImpl}
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.watcher.ConfigWatcher

/**
 * Component:
 * Description:
 * Date: 16/4/10
 *
 * @author Andy Ai
 */
trait ConfigSystem {

  def get[T](clazz: Class[T]): T

  def environment(): Environment

}

private[this] class ConfigSystemImpl(config: Config) extends ConfigSystem {
  private[this] val _environment = Environment(
    config.getString("dryad.host"),
    config.getInt("dryad.port"),
    config.getString("dryad.namesapce"),
    config.getString("dryad.group")
  )
  private[this] val configWatcher = {
    val cw = Class.forName(config.getString("dryad.watcher")).newInstance().asInstanceOf[ConfigWatcher]
    cw.config(_environment)
    cw
  }
  private[this] val configProvider = {
    val cp = Class.forName(config.getString("dryad.provider")).newInstance().asInstanceOf[ConfigProvider]
    cp.config(_environment)
    cp
  }
  private[this] val configService = new ConfigServiceImpl(configWatcher, configProvider)
  private[this] val configGarage = new ConfigGarageImpl(configService)

  override def get[T](clazz: Class[T]): T = {
    configGarage.get(clazz)
  }

  def environment(): Environment = _environment

}

object ConfigSystem {

  def apply(config: Config): ConfigSystem = new ConfigSystemImpl(config)

  def apply(): ConfigSystem = new ConfigSystemImpl(ConfigFactory.load("application.conf"))

}
