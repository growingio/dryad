package io.growing.dryad.internal

import java.util.concurrent.atomic.AtomicReference

/**
 * Component:
 * Description:
 * Date: 16/3/31
 *
 * @author Andy Ai
 */
abstract class ConfigReference[T] {
  private val reference = new AtomicReference[T]()

  def set(t: T): Unit = reference.set(t)
}
