package io.growing.dryad.cluster.rule

import java.util.concurrent.atomic.AtomicLong

import io.growing.dryad.registry.dto.Server

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
class RoundRobin extends LoadBalanceRule {

  private[this] val index = new AtomicLong(0)

  override def chooseServer(servers: Seq[Server], key: String): Server = {
    if (servers.size == 1) {
      servers.head
    } else {
      val _index = (index.getAndIncrement() % servers.size).toInt
      servers(_index)
    }
  }

}
