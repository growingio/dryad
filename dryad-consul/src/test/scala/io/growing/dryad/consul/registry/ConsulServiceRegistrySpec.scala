package io.growing.dryad.consul.registry

import com.typesafe.config.ConfigFactory
import io.growing.dryad.ServiceProviderImpl
import io.growing.dryad.registry.dto.Schema
import org.scalatest.funsuite.AnyFunSuite

/**
 * Component:
 * Description:
 * Date: 2018-12-21
 *
 * @author AI
 */
class ConsulServiceRegistrySpec extends AnyFunSuite {

  test("get instances") {
    val provider = new ServiceProviderImpl(ConfigFactory.load())
    provider.register()
    val instances = provider.getInstances(Schema.GRPC, "dryad-v2", None)
    assert(instances.isEmpty)
  }

}
