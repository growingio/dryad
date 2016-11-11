package io.growing.dryad.registry.dto

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
case class Service(id: String, name: String, schema: String, address: String, port: Int, pattern: String, group: String, ttl: Long)
