package io.growing.dryad.registry.dto

import io.growing.dryad.portal.Schema.Schema

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
final case class ServiceInstance(name: String, schema: Schema, address: String, port: Int)
