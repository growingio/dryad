package io.growing.dryad.internal

import java.util.concurrent.atomic.AtomicReference

/**
 * Component:
 * Description:
 * Date: 16/3/22
 *
 * @author Andy Ai
 */
trait Configuration {
  def name: String

  def payload: String

  def version: Long

  def namespace: String

  def group: String

}

final case class ConfigurationValue(name: String, payload: String, version: Long, namespace: String, group: String) extends Configuration

class ConfigurationReference(underlying: Configuration) extends Configuration {
  private val ref = new AtomicReference(underlying)

  override def name: String = ref.get.name

  override def payload: String = ref.get.payload

  override def version: Long = ref.get.version

  override def namespace: String = ref.get.namespace

  override def group: String = ref.get.group

  def set(underlying: Configuration): Unit = {
    ref.set(underlying)
  }
}
