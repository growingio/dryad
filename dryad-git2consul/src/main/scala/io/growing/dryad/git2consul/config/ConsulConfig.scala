package io.growing.dryad.git2consul.config

/**
 * Component:
 * Description:
 * Date: 2018-12-25
 *
 * @author AI
 */
final case class ConsulConfig(host: String, port: Int, connectTimeout: Option[Long], username: Option[String], password: Option[String])
