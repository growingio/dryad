package io.growing.dryad.reader

import java.util.Properties

import com.typesafe.config.Config

/**
 * Component:
 * Description:
 * Date: 16/4/9
 *
 * @author Andy Ai
 */
trait ConfigReader {

  def getInt(path: String): Int

  def getLong(path: String): Long

  def getDouble(path: String): Double

  def getString(path: String): String

  def getBoolean(path: String): Boolean

}

final class ConfReader(underlying: Config) extends ConfigReader {

  override def getInt(path: String): Int = underlying.getInt(path)

  override def getDouble(path: String): Double = underlying.getDouble(path)

  override def getLong(path: String): Long = underlying.getLong(path)

  override def getBoolean(path: String): Boolean = underlying.getBoolean(path)

  override def getString(path: String): String = underlying.getString(path)
}

final class PropsReader(underlying: Properties) extends ConfigReader {

  override def getInt(path: String): Int = underlying.getProperty(path).toInt

  override def getDouble(path: String): Double = underlying.getProperty(path).toDouble

  override def getLong(path: String): Long = underlying.getProperty(path).toLong

  override def getBoolean(path: String): Boolean = underlying.getProperty(path).toBoolean

  override def getString(path: String): String = underlying.getProperty(path)
}

object ConfigReader {

  def apply(obj: Object): ConfigReader = obj match {
    case conf: Config      ⇒ new ConfReader(conf)
    case props: Properties ⇒ new PropsReader(props)
    case _                 ⇒ new PropsReader(obj.asInstanceOf[Properties])
  }

}