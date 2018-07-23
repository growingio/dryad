package io.growing.dryad.registry

import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.registry.dto.{ Service, ServiceInstance }

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

  def subscribe(groups: Seq[String], schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit

  def getInstances(groups: Seq[String], schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[ServiceInstance]

}
