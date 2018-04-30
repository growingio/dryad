package io.growing.dryad.registry.dto

import io.growing.dryad.registry.HealthCheck
import io.growing.dryad.registry.dto.LoadBalancing.LoadBalancing

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
final case class Service(id: String, name: String, schema: String, address: String,
                         port: Int, pattern: String, group: String, check: HealthCheck,
                         priority: Int, loadBalancing: Option[LoadBalancing], nonCertifications: Seq[String], rpcPort: Option[Int])
