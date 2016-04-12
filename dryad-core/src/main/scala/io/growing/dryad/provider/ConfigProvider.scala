package io.growing.dryad.provider

import io.growing.dryad.inject.EnvironmentInjector
import io.growing.dryad.internal.Configuration

/**
 * Component:
 * Description:
 * Date: 16/3/26
 *
 * @author Andy Ai
 */
trait ConfigProvider extends EnvironmentInjector {

  def load(name: String): Configuration

}
