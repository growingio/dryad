package io.growing.dryad.provider

import java.math.BigInteger
import java.util
import java.util.concurrent.atomic.AtomicReference
import java.util.{ List ⇒ JList }

import com.google.common.base.{ Charsets, Optional }
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

  override def load(path: String): ConfigurationDesc = doLoad(path, None)

  override def load(path: String, listener: ConfigChangeListener): ConfigurationDesc = doLoad(path, Option(listener))

  private[this] def doLoad(path: String, listenerOpt: Option[ConfigChangeListener]): ConfigurationDesc = {
    val config = ConsulClient.kvClient.getValue(path)
    if (!config.isPresent) {
      throw new ConfigurationNotFoundException(path)
    }
    val version = config.get().getModifyIndex
    val payload = new String(BaseEncoding.base64().decode(config.get().getValue.get()), Charsets.UTF_8)
    listenerOpt.foreach(listener ⇒ addListener(path, listener))
    ConfigurationDesc(path, payload, version)
  }

  private[this] def addListener(path: String, listener: ConfigChangeListener): Unit = {
    listeners.get(path, () ⇒ new util.ArrayList[ConfigChangeListener]()).add(listener)
    watchers.get(path, () ⇒ new Watcher(path))
  }

  private[this] class Watcher(path: String) {
    private[this] val callback: ConsulResponseCallback[Optional[Value]] = new ConsulResponseCallback[Optional[Value]]() {
      private[this] val index = new AtomicReference[BigInteger]

      override def onComplete(consulResponse: ConsulResponse[Optional[Value]]): Unit = {
        if (consulResponse.getResponse.isPresent) {
          val value = consulResponse.getResponse.get()
          val payload = new String(BaseEncoding.base64().decode(value.getValue.get()), Charsets.UTF_8)
          val configuration = ConfigurationDesc(path, payload, value.getModifyIndex)
          listeners.getIfPresent(path).asScala.foreach { listener ⇒
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
    ConsulClient.kvClient.getValue(path, QueryOptions.blockMinutes(BLOCK_QUERY_MINS, new BigInteger("0")).build(), callback)
  }

}
