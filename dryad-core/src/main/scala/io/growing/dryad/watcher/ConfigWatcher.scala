package io.growing.dryad.watcher

import io.growing.dryad.internal.ConfigurationDesc
import rx.subjects.PublishSubject

/**
 * Component:
 * Description:
 * Date: 16/3/29
 *
 * @author Andy Ai
 */
trait ConfigWatcher {
  def watch(name: String): Unit

  def unwatch(name: String): Unit

  def awareSubject(subject: PublishSubject[ConfigurationDesc]): Unit
}
