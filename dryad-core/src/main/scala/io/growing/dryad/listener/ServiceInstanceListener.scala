package io.growing.dryad.listener

import io.growing.dryad.registry.dto.Server

/**
 * Component:
 * Description:
 * Date: 2018/7/19
 *
 * @author AI
 */
trait ServiceInstanceListener {

  def onChange(instances: Seq[Server]): Unit

}
