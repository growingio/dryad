package io.growing.dryad.cluster

import com.typesafe.config.ConfigFactory
import io.growing.dryad.ServiceProvider
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.registry.dto.ServiceInstance
import org.scalatest.FunSuite

/**
  *
  * Date: 2019-01-08
  *
  * @author AI
  */
class ClusterSpec extends FunSuite {

  test("cluster rb with service provider") {
    val port = 8080
    val provider = new ServiceProvider {
      override def register(): Unit = ???

      override def deregister(): Unit = ???

      override def register(patterns: (Schema, Seq[String])*): Unit = ???

      override def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit = ???

      override def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[ServiceInstance] = {
        Seq(
          ServiceInstance(serviceName, schema, "prod1", port),
          ServiceInstance(serviceName, schema, "prod2", port)
        )
      }
    }

    val cluster = Cluster(provider)
    val instances = Seq(
      cluster.roundRobin(Schema.GRPC, "grpc-service"),
      cluster.roundRobin(Schema.GRPC, "grpc-service"),
    )
    assert(instances.exists(_.address == "prod1"))
    assert(instances.exists(_.address == "prod2"))
  }

  test("cluster rb with direct service provider") {
    val cluster = Cluster.direct(ConfigFactory.load())
    val instances = Seq(
      cluster.roundRobin(Schema.GRPC, "grpc"),
      cluster.roundRobin(Schema.GRPC, "grpc"),
    )
    assert(instances.exists(_.address == "prod1"))
    assert(instances.exists(_.address == "prod2"))
  }

}
