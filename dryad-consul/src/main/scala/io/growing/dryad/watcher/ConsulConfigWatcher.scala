package io.growing.dryad.watcher

import java.math.BigInteger
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicReference

import com.google.common.base.Optional
import com.google.common.cache.CacheBuilder
import com.orbitz.consul.KeyValueClient
import com.orbitz.consul.async.ConsulResponseCallback
import com.orbitz.consul.model.ConsulResponse
import com.orbitz.consul.model.kv.Value
import com.orbitz.consul.option.QueryOptions
import io.growing.dryad.internal.{Configuration, ConfigurationValue}
import io.growing.dryad.{ConsulClient, Environment}
import rx.subjects.PublishSubject

/**
 * Component:
 * Description:
 * Date: 16/3/29
 *
 * @author Andy Ai
 */
class ConsulConfigWatcher extends ConfigWatcher {
  private val MAX_WATCHER_SIZE: Long = 5000
  private var _subject: PublishSubject[Configuration] = _
  private lazy val watchers = CacheBuilder.newBuilder().maximumSize(MAX_WATCHER_SIZE).build[String, Watcher]()
  private var environment: Environment = _

  override def config(environment: Environment): Unit = {
    this.environment = environment
  }

  override def unwatch(name: String): Unit = {
    watchers.invalidate(name)
  }

  override def watch(name: String): Unit = {
    watchers.get(name, new Callable[Watcher]() {
      override def call(): Watcher = {
        new Watcher(name, environment, _subject)
      }
    })
  }

  override def awareSubject(subject: PublishSubject[Configuration]): Unit = {
    _subject = subject
  }

}

private[this] class Watcher(name: String, environment: Environment, subject: PublishSubject[Configuration]) {
  private val path = ConsulClient.path(environment, name)
  private val kvClient = ConsulClient.client(environment)
  private val callback: ConsulResponseCallback[Optional[Value]] = new ConsulResponseCallback[Optional[Value]]() {
    private val index = new AtomicReference[BigInteger]

    override def onComplete(consulResponse: ConsulResponse[Optional[Value]]): Unit = {
      if (consulResponse.getResponse.isPresent) {
        val value = consulResponse.getResponse.get()
        subject.onNext(ConfigurationValue(name, None.orNull, value.getModifyIndex, environment.namespace, environment.group))
      }
      index.set(consulResponse.getIndex)
      watch()
    }

    override def onFailure(throwable: Throwable): Unit = {
      watch()
    }

    private def watch(): Unit = {
      kvClient.getValue(path, QueryOptions.blockMinutes(60 * 30, index.get()).build(), this)
    }
  }
  kvClient.getValue(name, QueryOptions.blockMinutes(60 * 30, new BigInteger("0")).build(), callback)
}
