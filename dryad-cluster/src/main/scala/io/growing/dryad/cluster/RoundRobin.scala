package io.growing.dryad.cluster

import java.util.concurrent.atomic.{ AtomicLong, AtomicReference }

import io.growing.dryad.registry.dto.ServiceInstance

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
final case class RoundRobin(name: String) {

  private[this] val index = new AtomicLong(0)
  private[this] val nodes = new AtomicReference[Seq[ServiceInstance]]()

  def setServiceInstance(instances: Seq[ServiceInstance]): RoundRobin = {
    nodes.set(instances)
    this
  }

  def get(): ServiceInstance = {
    val instances = nodes.get()
    if (instances.isEmpty) {
      throw new NoSuchElementException(s"$name instances is empty")
    }
    val _index = (index.getAndIncrement() % instances.size).toInt
    instances(_index)
  }
}
