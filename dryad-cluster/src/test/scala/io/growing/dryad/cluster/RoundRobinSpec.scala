package io.growing.dryad.cluster

import io.growing.dryad.registry.dto.{ Schema, ServiceInstance }
import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
class RoundRobinSpec extends FunSuite {

  test("LB") {
    val name = "test"
    val rr = RoundRobin(name)
    rr.setServiceInstance(Seq(
      ServiceInstance(name, Schema.HTTP, "10.0.0.1", 8080),
      ServiceInstance(name, Schema.HTTP, "10.0.0.2", 8080)))
    val instances = (1 to 10).map { _ ⇒
      rr.get()
    }
    assert(instances.count(_.address == "10.0.0.1") == 5)
  }

}
