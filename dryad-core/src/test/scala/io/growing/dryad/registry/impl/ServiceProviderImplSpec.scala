package io.growing.dryad.registry.impl

import com.typesafe.config.ConfigFactory
import io.growing.dryad.registry.{ GrpcHealthCheck, HttpHealthCheck }
import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018/5/1
 *
 * @author AI
 */
class ServiceProviderImplSpec extends FunSuite {

  test("Parse service") {
    val provider = new ServiceProviderImpl(ConfigFactory.load("services.conf"))
    val service = provider.service
    val httpPortalOpt = service.portals.find(_.schema == "http")
    assert(httpPortalOpt.isDefined)
    assert(httpPortalOpt.get.check.isInstanceOf[HttpHealthCheck])
    assert(httpPortalOpt.get.nonCertifications.contains("/internal/*"))
    assert(httpPortalOpt.get.port == 8083)
    val grpcPortalOpt = service.portals.find(_.schema == "grpc")
    assert(grpcPortalOpt.isDefined)
    assert(grpcPortalOpt.get.check.isInstanceOf[GrpcHealthCheck])
    assert(grpcPortalOpt.get.nonCertifications.contains("/rpc.internal.*"))
    assert(grpcPortalOpt.get.port == 9083)
  }

}
