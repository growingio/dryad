package io.growing.dryad.cluster

import java.util.concurrent.Callable

import com.google.common.cache.CacheBuilder
import com.google.common.hash.Hashing
import com.typesafe.config.Config
import io.growing.dryad.ServiceProvider
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.provider.DirectServiceProvider
import io.growing.dryad.registry.dto.ServiceInstance

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
trait Cluster {

  def roundRobin(schema: Schema, serviceName: String): ServiceInstance

}

object Cluster {

  def apply(provider: ServiceProvider): Cluster = new ClusterImpl(provider)

  def direct(config: Config): Cluster = new ClusterImpl(new DirectServiceProvider(config))

}

class ClusterImpl(provider: ServiceProvider) extends Cluster {
  private[this] val clusters = CacheBuilder.newBuilder().build[String, RoundRobin]()

  override def roundRobin(schema: Schema, serviceName: String): ServiceInstance = {
    val name = s"$serviceName-$schema"
    clusters.get(name, new Callable[RoundRobin] {
      override def call(): RoundRobin = {
        val rr = RoundRobin(name)
        val instances = provider.getInstances(schema, serviceName, Option(new ClusterServiceInstanceListener(rr)))
        rr.setServiceInstance(sortedInstances(instances))
      }
    }).get()
  }

  private[cluster] def sortedInstances(instances: Seq[ServiceInstance]): Seq[ServiceInstance] = {
    if (instances.size > 1) {
      instances.sortBy { instance ⇒
        val hashCode = s"${instance.address}:${instance.port}".hashCode
        Hashing.consistentHash(hashCode, instances.size)
      }
    } else {
      instances
    }
  }

  private[cluster] class ClusterServiceInstanceListener(rr: RoundRobin) extends ServiceInstanceListener {
    override def onChange(instances: Seq[ServiceInstance]): Unit = {
      rr.setServiceInstance(sortedInstances(instances))
    }
  }

}
