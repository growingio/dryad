package io.growing.dryad.cluster.rule

import io.growing.dryad.registry.dto.Server

/**
 *
 * @author AI
 *         2020/2/8
 */
trait LoadBalanceRule {

  def chooseServer(servers: Seq[Server], key: String): Server

}
