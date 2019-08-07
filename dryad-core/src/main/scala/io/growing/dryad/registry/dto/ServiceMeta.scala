package io.growing.dryad.registry.dto

import io.growing.dryad.registry.dto.LoadBalancing.LoadBalancing

/**
 *
 * @author AI
 *         2019-08-06
 */
final case class ServiceMeta(pattern: String, nonCertifications: Seq[String] = Seq.empty, loadBalancing: Option[LoadBalancing] = None)
