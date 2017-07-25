package io.growing.dryad

import com.typesafe.config.{ Config, ConfigFactory }
import io.growing.dryad.internal.ConfigService
import io.growing.dryad.provider.ConfigProvider

import scala.reflect.ClassTag

/**
 * Component:
 * Description:
 * Date: 16/4/10
 *
 * @author Andy Ai
 */
trait ConfigSystem {

  def group: String

  def namespace: String

  def configuration: Config

  def get[T: ClassTag]: T

  def get(name: String, ignoreGroup: Boolean = false): Config

}

object ConfigSystem {

  def apply(): ConfigSystem = new ConfigSystemImpl(ConfigFactory.load())

  def apply(config: Config): ConfigSystem = new ConfigSystemImpl(config)

}

private[this] class ConfigSystemImpl(config: Config) extends ConfigSystem {
  private[this] val _group: String = config.getString("dryad.group")
  private[this] val _namespace: String = config.getString("dryad.namespace")
  private[this] val provider: ConfigProvider = {
    val name = config.getString("dryad.provider")
    Class.forName(name).newInstance().asInstanceOf[ConfigProvider]
  }
  private[this] val configServer: ConfigService = ConfigService(provider)

  override def group: String = _group

  override def namespace: String = _namespace

  override def configuration: Config = config

  override def get[T: ClassTag]: T = configServer.get[T](_namespace, _group)

  override def get(name: String, ignoreGroup: Boolean = false): Config = {
    configServer.get(name, _namespace, if (ignoreGroup) None else Option(_group))
  }

}

