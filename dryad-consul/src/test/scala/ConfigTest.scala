import java.util.UUID

import io.growing.dryad.ConfigSystem
import io.growing.dryad.client.ConsulClient
import org.scalatest._

@Ignore class ConfigTest extends FunSuite {

  test("Consul client") {
    val configSystem = ConfigSystem()
    val group = configSystem.group
    val namespace = configSystem.namespace
    ConsulClient.kvClient.putValue(
      ConsulClient.path(namespace, group, "application.conf"),
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin
    )
    val config = configSystem.get[ApplicationConfig]
    assertResult(18)(config.age)
    assertResult("Andy.Ai")(config.name)

    for (i ← 1 to 10) {
      val name = UUID.randomUUID().toString
      ConsulClient.kvClient.putValue(
        ConsulClient.path(namespace, group, "application.conf"),
        s"""
        {
          name: "$name"
          age: $i
        }
        """.stripMargin
      )

      Thread.sleep(20)

      assertResult(i)(config.age)
      assertResult(name)(config.name)
    }

    ConsulClient.kvClient.deleteKey(ConsulClient.path(namespace, group, "application.conf"))
  }

  test("Consul client2") {
    val configSystem = ConfigSystem()
    val group = configSystem.group
    val namespace = configSystem.namespace
    ConsulClient.kvClient.putValue(
      ConsulClient.path(namespace, group, "application.conf"),
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin
    )
    val config = configSystem.get[ApplicationConfig]
    assertResult(18)(config.age)
    assertResult("Andy.Ai")(config.name)

    Thread.sleep(1000 * 60 * 5)

    for (i ← 1 to 100000000) {
      println(s"name: ${config.name}, age: ${config.age}")
      Thread.sleep(1000)
    }

    ConsulClient.kvClient.deleteKey(ConsulClient.path(namespace, group, "application.conf"))
  }
}
