package io.growing.dryad.registry

import io.growing.dryad.registry.dto.Service

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
trait ServiceRegistry {

  def register(service: Service): Unit

  def deregister(service: Service): Unit

}
