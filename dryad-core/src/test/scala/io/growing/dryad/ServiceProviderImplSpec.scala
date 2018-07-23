package io.growing.dryad

import com.typesafe.config.ConfigFactory
import io.growing.dryad.portal.Schema
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
    provider.initService(Seq.empty)
    val service = provider.getService
    val httpPortalOpt = service.portals.find(_.schema == Schema.HTTP)
    assert(httpPortalOpt.isDefined)
    assert(httpPortalOpt.get.check.isInstanceOf[HttpHealthCheck])
    assert(httpPortalOpt.get.nonCertifications.contains("/internal/*"))
    assert(httpPortalOpt.get.port == 8083)
    val grpcPortalOpt = service.portals.find(_.schema == Schema.GRPC)
    assert(grpcPortalOpt.isDefined)
    assert(grpcPortalOpt.get.check.isInstanceOf[GrpcHealthCheck])
    assert(grpcPortalOpt.get.nonCertifications.contains("/rpc.internal.*"))
    assert(grpcPortalOpt.get.port == 9083)
  }

}
