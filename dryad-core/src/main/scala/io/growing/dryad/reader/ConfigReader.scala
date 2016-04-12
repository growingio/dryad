package io.growing.dryad.reader

import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.config.ConfigException.{Missing, WrongType}
import com.typesafe.config.impl.DescriptionConfigOrigin

import scala.util.control.NonFatal

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

final class MapReader(underlying: Map[String, String]) extends ConfigReader {

  override def getInt(path: String): Int = read(path, _.toInt)

  override def getDouble(path: String): Double = read(path, _.toDouble)

  override def getLong(path: String): Long = read(path, _.toLong)

  override def getBoolean(path: String): Boolean = read(path, _.toBoolean)

  override def getString(path: String): String = read[String](path, v ⇒ v)

  private[this] def read[T: Manifest](path: String, f: (String) ⇒ T): T = {
    underlying.get(path) match {
      case None ⇒ throw new Missing(path)
      case Some(value) ⇒
        try {
          f(value)
        } catch {
          case NonFatal(t) ⇒ throw new WrongType(
            new DescriptionConfigOrigin(t.getLocalizedMessage), path, manifest[T].getClass.getName, value
          )
        }
    }
  }
}

object ConfigReader {

  def apply(obj: Object): ConfigReader = obj match {
    case conf: Config             ⇒ new ConfReader(conf)
    case props: Properties        ⇒ new PropsReader(props)
    case map: Map[String, String] ⇒ new MapReader(map)
    case others                   ⇒ throw new UnsupportedOperationException(s"Unsupported read ${others.getClass}")
  }

}