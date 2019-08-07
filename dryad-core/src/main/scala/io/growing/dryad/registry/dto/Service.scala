package io.growing.dryad.registry.dto

import io.growing.dryad.registry.HealthCheck
import io.growing.dryad.registry.dto.Schema.Schema

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
final case class Service(id: String, name: String, schema: Schema, address: String,
                         port: Int, group: String, priority: Int, meta: ServiceMeta, check: HealthCheck) {

  def withPatterns(patterns: Iterable[String]): Service = {
    copy(meta = meta.copy(pattern = patterns.mkString(",")))
  }

}
