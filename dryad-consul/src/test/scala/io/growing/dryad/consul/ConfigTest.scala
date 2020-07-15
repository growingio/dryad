package io.growing.dryad.consul

import java.util.UUID

import io.growing.dryad.ConfigSystem
import io.growing.dryad.consul.client.ConsulClient
import org.scalatest.funsuite.AnyFunSuite

class ConfigTest extends AnyFunSuite {

  test("Consul client") {
    val configSystem = ConfigSystem()
    val group = configSystem.group
    val namespace = configSystem.namespace
    val path = Seq(namespace, group, "application.conf").mkString("/")
    ConsulClient.kvClient.putValue(
      path,
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin)
    val config = configSystem.get[ApplicationConfig]
    assertResult(18)(config.age)
    assertResult("Andy.Ai")(config.name)

    for (i ← 1 to 10) {
      val name = UUID.randomUUID().toString
      ConsulClient.kvClient.putValue(
        path,
        s"""
        {
          name: "$name"
          age: $i
        }
        """.stripMargin)

      Thread.sleep(20)
      assertResult(i)(config.age)
      assertResult(name)(config.name)
    }

    ConsulClient.kvClient.deleteKey(path)
  }

  test("Consul client2") {
    val configSystem = ConfigSystem()
    val group = configSystem.group
    val namespace = configSystem.namespace
    val path = Seq(namespace, group, "application.conf").mkString("/")
    ConsulClient.kvClient.putValue(
      path,
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin)
    val config = configSystem.get[ApplicationConfig]
    assertResult(18)(config.age)
    assertResult("Andy.Ai")(config.name)
    ConsulClient.kvClient.deleteKey(path)
  }

  test("Consul client3") {
    val configSystem = ConfigSystem()
    val namespace = configSystem.namespace
    val path = Seq(namespace, "application.conf").mkString("/")
    ConsulClient.kvClient.putValue(
      path,
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin)
    val config = configSystem.get[ApplicationConfig2]
    assertResult(18)(config.age)
    assertResult("Andy.Ai")(config.name)

    for (i ← 1 to 10) {
      val name = UUID.randomUUID().toString
      ConsulClient.kvClient.putValue(
        path,
        s"""
        {
          name: "$name"
          age: $i
        }
        """.stripMargin)

      Thread.sleep(20)

      assertResult(i)(config.age)
      assertResult(name)(config.name)
    }

    ConsulClient.kvClient.deleteKey(path)
  }

  test("Consul client4") {
    val configSystem = ConfigSystem()
    val namespace = configSystem.namespace
    val path = Seq(namespace, "application.conf").mkString("/")
    ConsulClient.kvClient.putValue(
      path,
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin)

    val configStr = configSystem.getConfigAsStringRecursive("a/b/c/d/application.conf")
    assert(configStr.contains("Andy.Ai"))

    ConsulClient.kvClient.deleteKey(path)
  }
}
