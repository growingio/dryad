package io.growing.dryad.cluster

import com.typesafe.config.Config
import io.growing.dryad.ServiceProvider
import io.growing.dryad.cluster.impl.ClusterImpl
import io.growing.dryad.provider.DirectServiceProvider
import io.growing.dryad.registry.dto.Schema.Schema
import io.growing.dryad.registry.dto.Server
import io.growing.dryad.utils.ConfigUtils._

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
trait Cluster {

  def makeServerDown(server: Server): Unit

  //  def onError(server: Server, throwable: Throwable): Unit

  def roundRobin(schema: Schema, serverName: String): Server

  //  def reachableServers(schema: Schema, serverName: String): Seq[Server]

}

object Cluster {

  def apply(provider: ServiceProvider): Cluster = new ClusterImpl(provider, 0)

  def direct(config: Config): Cluster = {
    lazy val defaultInterval = 5
    val interval = config.getIntOpt("cluster.check.interval").getOrElse(defaultInterval)
    new ClusterImpl(new DirectServiceProvider(config), interval)
  }

}
