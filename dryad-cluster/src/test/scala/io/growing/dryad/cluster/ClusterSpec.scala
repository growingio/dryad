package io.growing.dryad.cluster

import com.typesafe.config.ConfigFactory
import io.growing.dryad.ServiceProvider
import io.growing.dryad.cluster.impl.ClusterImpl
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.provider.DirectServiceProvider
import io.growing.dryad.registry.dto.Schema
import io.growing.dryad.registry.dto.Schema.Schema
import io.growing.dryad.registry.dto.Server
import io.growing.dryad.registry.dto.Service
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import org.scalatest.funsuite.AnyFunSuite

/**
 *
 * Date: 2019-01-08
 *
 * @author AI
 */
class ClusterSpec extends AnyFunSuite {

  test("cluster rb with service provider") {
    val port = 8080
    val provider = new ServiceProvider {
      override def register(): Unit = ???

      override def deregister(): Unit = ???

      override def getServices: Set[Service] = ???

      override def addPatterns(schema: Schema, patterns: String*): Unit = ???

      override def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit = ???

      override def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[Server] = {
        Seq(
          Server(serviceName, schema, "prod1", port),
          Server(serviceName, schema, "prod2", port))
      }
    }

    val cluster = Cluster(provider)
    val instances = Seq(
      cluster.roundRobin(Schema.GRPC, "grpc-service"),
      cluster.roundRobin(Schema.GRPC, "grpc-service"))
    assert(instances.exists(_.address == "prod1"))
    assert(instances.exists(_.address == "prod2"))
  }

  test("cluster rb with direct service provider") {
    val cluster = Cluster.direct(ConfigFactory.load())
    val instances = Seq(
      cluster.roundRobin(Schema.GRPC, "grpc"),
      cluster.roundRobin(Schema.GRPC, "grpc"))
    assert(instances.exists(_.address == "0.0.0.0"))
    assert(instances.exists(_.address == "prod2"))
  }

  test("target is available") {
    val cluster = Cluster.direct(ConfigFactory.load()).asInstanceOf[ClusterImpl]
    assert(cluster.isAvailable(Server("GrowingIO", Schema.HTTP, "www.growingio.com", 80)))
    assert(!cluster.isAvailable(Server("GrowingIO", Schema.HTTP, "www.growingio.com", 81)))
  }

  test("make server down") {
    val cluster = Cluster.direct(ConfigFactory.load()).asInstanceOf[ClusterImpl]
    val server = cluster.roundRobin(Schema.GRPC, "grpc")
    cluster.makeServerDown(server)
    (1 to 5).foreach { _ ⇒
      val s = cluster.roundRobin(Schema.GRPC, "grpc")
      assert(s.address != server.address)
    }
  }

  test("check timer") {
    val undertow = Undertow.builder().addHttpListener(8080, "0.0.0.0").setHandler(new HttpHandler {
      override def handleRequest(exchange: HttpServerExchange): Unit = {
        exchange.getResponseSender.send("imok")
        exchange.endExchange()
      }
    }).build()
    undertow.start()
    val cluster = new ClusterImpl(new DirectServiceProvider(ConfigFactory.load()), 1)
    cluster.makeServerDown(Server("grpc", Schema.GRPC, "0.0.0.0", 8080))
    cluster.roundRobin(Schema.GRPC, "grpc")
    Thread.sleep(2000)
    val servers = (1 to 5).map { _ ⇒
      cluster.roundRobin(Schema.GRPC, "grpc")
    }
    assert(servers.exists(s ⇒ s.address == "0.0.0.0" && s.port == 8080))
    undertow.stop()
  }

}
