package io.growing.dryad.provider

import com.typesafe.config.Config
import io.growing.dryad.ServiceProvider
import io.growing.dryad.listener.ServiceInstanceListener
import io.growing.dryad.portal.Schema.Schema
import io.growing.dryad.registry.dto.ServiceInstance
import scala.collection.JavaConverters._

/**
 *
 * Date: 2019-01-08
 *
 * @author AI
 */
class DirectServiceProvider(config: Config) extends ServiceProvider {

  override def register(): Unit = ???

  override def deregister(): Unit = ???

  override def register(patterns: (Schema, Seq[String])*): Unit = ???

  override def subscribe(schema: Schema, serviceName: String, listener: ServiceInstanceListener): Unit = ???

  override def getInstances(schema: Schema, serviceName: String, listener: Option[ServiceInstanceListener]): Seq[ServiceInstance] = {
    config.getConfigList(s"cluster.providers.$serviceName").asScala.map { nodeConfig ⇒
      ServiceInstance(serviceName, schema, nodeConfig.getString("address"), nodeConfig.getInt("port"))
    }
  }

}
