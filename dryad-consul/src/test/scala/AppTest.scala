import java.util.UUID

import io.growing.dryad.{ConfigSystem, ConsulClient, Environment}
import org.scalatest._

class AppTest extends FlatSpec with Matchers {
  "Default test" should "Print something" in {
    val configSystem = ConfigSystem()
    val environment: Environment = configSystem.environment()
    ConsulClient.client(environment).putValue(
      ConsulClient.path(environment, "application.conf"),
      """
        {
          name: "Andy.Ai"
          age: 18
        }
      """.stripMargin
    )
    val config = configSystem.get(classOf[ApplicationConfig])
    assertResult(18)(config.age)
    assertResult("Andy.Ai")(config.name)

    for (i ← 1 to 200) {
      val name = UUID.randomUUID().toString
      ConsulClient.client(environment).putValue(
        ConsulClient.path(environment, "application.conf"),
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

    ConsulClient.client(environment).deleteKey(ConsulClient.path(environment, "application.conf"))
  }

}
