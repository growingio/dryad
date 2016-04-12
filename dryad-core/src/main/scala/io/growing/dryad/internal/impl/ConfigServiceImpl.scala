package io.growing.dryad.internal.impl

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader}
import io.growing.dryad.exception.ConfigurationNotFoundException
import io.growing.dryad.internal.{ConfigService, Configuration, ConfigurationReference}
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.snapshot.LocalFileConfigSnapshot
import io.growing.dryad.watcher.ConfigWatcher
import rx.lang.scala.Observer
import rx.subjects.PublishSubject

import scala.util.{Failure, Success, Try}

/**
 * Component:
 * Description:
 * Date: 16/3/26
 *
 * @author Andy Ai
 */
class ConfigServiceImpl(
  configWatcher:  ConfigWatcher,
  configProvider: ConfigProvider
) extends ConfigService with Observer[Configuration] {

  private[this] val MAX_CONFIG_CACHE_SIZE = 5000
  private[this] val configLoader = new CacheLoader[String, ConfigurationReference] {
    override def load(key: String): ConfigurationReference = {
      new ConfigurationReference(loadConfiguration(key))
    }
  }
  private[this] val configsCache = CacheBuilder.newBuilder()
    .softValues()
    .expireAfterAccess(60, TimeUnit.MINUTES)
    .maximumSize(MAX_CONFIG_CACHE_SIZE)
    .build(configLoader)
  private[this] val _subject = {
    val _subject = rx.lang.scala.subjects.PublishSubject[Configuration]()
    _subject.subscribe(this)
    _subject
  }
  configWatcher.awareSubject(_subject.asJavaSubject)

  override def get(name: String): Configuration = {
    Try(configsCache.get(name)) match {
      case Success(configuration) ⇒ configuration
      case Failure(e)             ⇒ throw new ConfigurationNotFoundException(name)
    }
  }

  override def subject(): PublishSubject[Configuration] = _subject.asJavaSubject

  override def onNext(value: Configuration): Unit = {
    Try(configsCache.get(value.name)) match {
      case Success(configuration) ⇒
        if (configuration.version < value.version) {
          configuration.set(loadConfiguration(value.name))
        }
      case Failure(e) ⇒ throw new ConfigurationNotFoundException(value.name)
    }
  }

  private[this] def loadConfiguration(name: String): Configuration = {
    val configuration = configProvider.load(name)
    LocalFileConfigSnapshot.flash(configuration)
    configWatcher.watch(name)
    configuration
  }
}
