package io.growing.dryad.registry

import scala.concurrent.duration.Duration

/**
 * Component:
 * Description:
 * Date: 2016/11/15
 *
 * @author Andy Ai
 */
sealed trait HealthCheck {
  val deregisterCriticalServiceAfter: Duration
}

final case class TTLHealthCheck(ttl: Duration, deregisterCriticalServiceAfter: Duration) extends HealthCheck

final case class HttpHealthCheck(url: String, interval: Duration,
                                 timeout: Duration, deregisterCriticalServiceAfter: Duration) extends HealthCheck

final case class GrpcHealthCheck(grpc: String, interval: Duration,
                                 useTls: Boolean, deregisterCriticalServiceAfter: Duration) extends HealthCheck
