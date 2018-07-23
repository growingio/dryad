package io.growing.dryad.cluster

import io.growing.dryad.ServiceProvider
import io.growing.dryad.portal.Schema
import org.scalatest.{ FunSuite, Ignore }

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
@Ignore
class ClusterSpec extends FunSuite {

  test("get instance") {
    val provider = ServiceProvider()
    val cluster = Cluster(provider)
    (1 to 15).foreach { _ ⇒
      val instance = cluster.roundRobin(Schema.HTTP, "growing-example")
      println(instance)
      Thread.sleep(1000)
    }
  }

}
