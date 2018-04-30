package io.growing.dryad.registry.dto

import io.growing.dryad.registry.dto.LoadBalancing.LoadBalancing

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
final case class Service(name: String, address: String,
                         group: String, portals: Set[Portal],
                         priority: Int, loadBalancing: Option[LoadBalancing])
