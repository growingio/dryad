package io.growing.dryad.registry.dto

import io.growing.dryad.registry.HealthCheck

/**
 * Component:
 * Description:
 * Date: 2018/5/1
 *
 * @author AI
 */
final case class Portal(id: String, schema: String, port: Int, pattern: String, check: HealthCheck, nonCertifications: Seq[String])
