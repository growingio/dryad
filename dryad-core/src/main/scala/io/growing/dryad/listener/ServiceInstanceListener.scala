package io.growing.dryad.listener

import io.growing.dryad.registry.dto.ServiceInstance

/**
 * Component:
 * Description:
 * Date: 2018/7/19
 *
 * @author AI
 */
trait ServiceInstanceListener {

  def onChange(instances: Seq[ServiceInstance]): Unit

}
