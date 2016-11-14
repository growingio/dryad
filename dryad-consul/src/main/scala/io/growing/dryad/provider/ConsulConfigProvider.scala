package io.growing.dryad.provider

import java.math.BigInteger
import java.util
import java.util.concurrent.atomic.AtomicReference
import java.util.{List ⇒ JList}

import com.google.common.base.{Charsets, Optional}
import com.google.common.cache.CacheBuilder
import com.google.common.io.BaseEncoding
import com.orbitz.consul.async.ConsulResponseCallback
import com.orbitz.consul.model.ConsulResponse
import com.orbitz.consul.model.kv.Value
import com.orbitz.consul.option.QueryOptions
import io.growing.dryad.client.ConsulClient
import io.growing.dryad.exception.ConfigurationNotFoundException
import io.growing.dryad.internal.ConfigurationDesc
import io.growing.dryad.watcher.ConfigChangeListener

import scala.collection.JavaConverters._

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
class ConsulConfigProvider extends ConfigProvider {
  private[this] val BLOCK_QUERY_MINS = 5
  private[this] val watchers = CacheBuilder.newBuilder().build[String, Watcher]()
  private[this] val listeners = CacheBuilder.newBuilder().build[String, JList[ConfigChangeListener]]()

  override def load(name: String, namespace: String, group: String, listener: ConfigChangeListener): ConfigurationDesc = {
    val path = ConsulClient.path(namespace, group, name)
    val config = ConsulClient.kvClient.getValue(path)
    if (!config.isPresent) {
      throw new ConfigurationNotFoundException(path)
    }
    val version = config.get().getModifyIndex
    val payload = new String(BaseEncoding.base64().decode(config.get().getValue.get()), Charsets.UTF_8)
    addListener(namespace, group, name, listener)
    ConfigurationDesc(name, payload, version, namespace, group)
  }

  private[this] def addListener(namespace: String, group: String, name: String, listener: ConfigChangeListener): Unit = {
    listeners.get(name, () ⇒ new util.ArrayList[ConfigChangeListener]()).add(listener)
    watchers.get(name, () ⇒ new Watcher(namespace, group, name))
  }

  private[this] class Watcher(namespace: String, group: String, name: String) {
    private[this] val path = ConsulClient.path(namespace, group, name)
    private[this] val callback: ConsulResponseCallback[Optional[Value]] = new ConsulResponseCallback[Optional[Value]]() {
      private[this] val index = new AtomicReference[BigInteger]

      override def onComplete(consulResponse: ConsulResponse[Optional[Value]]): Unit = {
        if (consulResponse.getResponse.isPresent) {
          val value = consulResponse.getResponse.get()
          val payload = new String(BaseEncoding.base64().decode(value.getValue.get()), Charsets.UTF_8)
          val configuration = ConfigurationDesc(name, payload, value.getModifyIndex, namespace, group)
          listeners.getIfPresent(name).asScala.foreach { listener ⇒
            listener.onChange(configuration)
          }
        }
        index.set(consulResponse.getIndex)
        watch()
      }

      override def onFailure(throwable: Throwable): Unit = {
        watch()
      }

      private def watch(): Unit = {
        ConsulClient.kvClient.getValue(path, QueryOptions.blockMinutes(BLOCK_QUERY_MINS, index.get()).build(), this)
      }
    }
    ConsulClient.kvClient.getValue(name, QueryOptions.blockMinutes(BLOCK_QUERY_MINS, new BigInteger("0")).build(), callback)
  }

}
