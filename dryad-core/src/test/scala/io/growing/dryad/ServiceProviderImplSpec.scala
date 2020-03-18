package io.growing.dryad

import com.typesafe.config.ConfigFactory
import io.growing.dryad.registry.GrpcHealthCheck
import io.growing.dryad.registry.HttpHealthCheck
import io.growing.dryad.registry.dto.Schema
import org.scalatest.funsuite.AnyFunSuite

/**
 * Component:
 * Description:
 * Date: 2018/5/1
 *
 * @author AI
 */
class ServiceProviderImplSpec extends AnyFunSuite {

  test("Parse service") {
    val provider = new ServiceProviderImpl(ConfigFactory.load("services.conf"))
    val services = provider.getServices
    val httpPortalOpt = services.find(_.schema == Schema.HTTP)
    assert(httpPortalOpt.isDefined)
    assert(httpPortalOpt.get.check.isInstanceOf[HttpHealthCheck])
    assert(httpPortalOpt.get.meta.nonCertifications.contains("/internal/*"))
    assert(httpPortalOpt.get.port == 8083)
    val grpcPortalOpt = services.find(_.schema == Schema.GRPC)
    assert(grpcPortalOpt.isDefined)
    assert(grpcPortalOpt.get.check.isInstanceOf[GrpcHealthCheck])
    assert(grpcPortalOpt.get.meta.nonCertifications.contains("/rpc.internal.*"))
    assert(grpcPortalOpt.get.port == 9083)
  }

  test("Set patterns") {
    val provider = new ServiceProviderImpl(ConfigFactory.load("services.conf"))
    provider.setPatterns(Schema.HTTP, "/projects/*")
    val services = provider.getServices
    val httpServiceOpt = services.find(_.schema == Schema.HTTP)
    assert(httpServiceOpt.isDefined)
    assert(httpServiceOpt.get.meta.pattern == "/projects/*")
  }

  test("parse default http check") {
    val config = ConfigFactory.parseString(
      s"""
         |dryad {
         |
 |  group = "dev"
         |  namespace = "default"
         |
 |  service {
         |    priority = 10
         |    address = "0.0.0.0"
         |    load-balancing = "url_chash"
         |
 |    http {
         |      port = 8083
         |      non-certifications = ["/internal/*"]
         |      check {
         |        interval = 10s
         |      }
         |    }
         |
         |
 |  }
         |}
       """.stripMargin)
    val provider = new ServiceProviderImpl(config)
    val services = provider.getServices
    val httpPortal = services.find(_.schema == Schema.HTTP).get
    val httpCheck = httpPortal.check.asInstanceOf[HttpHealthCheck]
    assert(httpCheck.url == s"http://0.0.0.0:8083/${httpPortal.id}/check")
  }

}
