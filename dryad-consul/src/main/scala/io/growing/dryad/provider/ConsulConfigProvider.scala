package io.growing.dryad.provider

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import io.growing.dryad.exception.ConfigurationNotFoundException
import io.growing.dryad.internal.{Configuration, ConfigurationValue}
import io.growing.dryad.{ConsulClient, Environment}

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
class ConsulConfigProvider extends ConfigProvider {
  private[this] var environment: Environment = _

  override def config(environment: Environment): Unit = {
    this.environment = environment
  }

  override def load(name: String): Configuration = {
    val path = ConsulClient.path(environment, name)
    val config = ConsulClient.client(environment).getValue(path)
    if (!config.isPresent) {
      throw new ConfigurationNotFoundException(path)
    }
    val version = config.get().getModifyIndex
    val payload = new String(BaseEncoding.base64().decode(config.get().getValue.get()), Charsets.UTF_8)
    ConfigurationValue(name, payload, version, environment.namespace, environment.group)
  }

}
