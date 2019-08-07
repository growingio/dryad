package io.growing.dryad.utils

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{ Failure, Success, Try }

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
object ConfigUtils {

  implicit class ConfigWrapper(config: Config) {

    implicit def getIntOpt(path: String): Option[Int] = getOpt(config.getInt(path))

    implicit def getLongOpt(path: String): Option[Long] = getOpt(config.getLong(path))

    implicit def getStringOpt(path: String): Option[String] = getOpt(config.getString(path))

    implicit def getConfigOpt(path: String): Option[Config] = getOpt(config.getConfig(path))

    implicit def getBooleanOpt(path: String): Option[Boolean] = getOpt(config.getBoolean(path))

    implicit def getDurationOpt(path: String): Option[Duration] = getOpt(config.getDuration(path)).map(_.toMillis.milliseconds)

    implicit def getStringSeqOpt(path: String): Option[Seq[String]] = getOpt(config.getStringList(path).asScala)

    private[this] def getOpt[T](f: ⇒ T): Option[T] = {
      Try(f) match {
        case Success(value) ⇒ Some(value)
        case Failure(_)     ⇒ None
      }
    }
  }

}
