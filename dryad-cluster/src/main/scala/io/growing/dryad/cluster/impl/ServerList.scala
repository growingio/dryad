package io.growing.dryad.cluster.impl

import java.util.concurrent.atomic.AtomicReference

import com.google.common.hash.Hashing
import io.growing.dryad.registry.dto.Server

/**
 *
 * @author AI
 *         2020/2/8
 */
class ServerList {

  private[this] val nodes: AtomicReference[Seq[Server]] = new AtomicReference[Seq[Server]]()

  def getAllServers: Seq[Server] = nodes.get()

  def setAllServers(servers: Seq[Server]): ServerList = {
    nodes.set(sortedServers(servers))
    this
  }

  private[impl] def sortedServers(servers: Seq[Server]): Seq[Server] = {
    if (servers.size > 1) {
      servers.sortBy { server ⇒
        val hashCode = s"${server.address}:${server.port}".hashCode
        Hashing.consistentHash(hashCode, servers.size)
      }
    } else {
      servers
    }
  }

}
