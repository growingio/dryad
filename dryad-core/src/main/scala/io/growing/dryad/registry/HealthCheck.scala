package io.growing.dryad.registry

/**
 * Component:
 * Description:
 * Date: 2016/11/15
 *
 * @author Andy Ai
 */
sealed trait HealthCheck

case class TTLHealthCheck(ttl: Long) extends HealthCheck

case class HttpHealthCheck(url: String, interval: Long, timeout: Long) extends HealthCheck

case class GrpcHealthCheck(grpc: String, interval: Long, useTls: Boolean) extends HealthCheck
