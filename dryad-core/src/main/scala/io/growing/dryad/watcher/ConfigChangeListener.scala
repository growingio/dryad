package io.growing.dryad.watcher

import io.growing.dryad.internal.ConfigurationDesc

/**
 * Component:
 * Description:
 * Date: 2016/10/26
 *
 * @author Andy Ai
 */
trait ConfigChangeListener {

  def onChange(configuration: ConfigurationDesc): Unit

}
