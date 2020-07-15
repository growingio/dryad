package io.growing.dryad.consul

import io.growing.dryad.ServiceProvider
import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite

class ServiceTest extends AnyFunSuite {

  private val provider = ServiceProvider()

  test("Register service") {
    provider.register()
  }

  test("Deregister service") {
    provider.deregister()
  }

}
