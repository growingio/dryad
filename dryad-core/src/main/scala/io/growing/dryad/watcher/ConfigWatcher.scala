package io.growing.dryad.watcher

import io.growing.dryad.inject.EnvironmentInjector
import io.growing.dryad.internal.Configuration
import rx.subjects.PublishSubject

/**
 * Component:
 * Description:
 * Date: 16/3/29
 *
 * @author Andy Ai
 */
trait ConfigWatcher extends EnvironmentInjector {
  def watch(name: String): Unit

  def unwatch(name: String): Unit

  def awareSubject(subject: PublishSubject[Configuration]): Unit
}
