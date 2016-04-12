package io.growing.dryad.inject

import io.growing.dryad.Environment

/**
 * Component:
 * Description:
 * Date: 16/4/10
 *
 * @author Andy Ai
 */
trait EnvironmentInjector {

  def config(environment: Environment): Unit

}
