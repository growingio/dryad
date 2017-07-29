package io.growing.dryad.provider

import io.growing.dryad.internal.ConfigurationDesc
import io.growing.dryad.watcher.ConfigChangeListener

/**
 * Component:
 * Description:
 * Date: 16/3/26
 *
 * @author Andy Ai
 */
trait ConfigProvider {

  def load(path: String): ConfigurationDesc

  def load(path: String, listener: ConfigChangeListener): ConfigurationDesc

}
