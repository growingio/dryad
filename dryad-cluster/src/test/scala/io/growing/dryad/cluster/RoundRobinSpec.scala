package io.growing.dryad.cluster

import io.growing.dryad.cluster.rule.RoundRobin
import io.growing.dryad.registry.dto.Schema
import io.growing.dryad.registry.dto.Server
import org.scalatest.funsuite.AnyFunSuite

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
class RoundRobinSpec extends AnyFunSuite {

  test("LB") {
    val name = "test"
    val rr = new RoundRobin
    val servers = Seq(
      Server(name, Schema.HTTP, "10.0.0.1", 8080),
      Server(name, Schema.HTTP, "10.0.0.2", 8080))
    val instances = (1 to 10).map { _ ⇒
      rr.chooseServer(servers, "")
    }
    assert(instances.count(_.address == "10.0.0.1") == 5)
  }

}
