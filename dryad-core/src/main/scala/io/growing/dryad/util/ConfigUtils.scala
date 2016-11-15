package io.growing.dryad.util

import com.typesafe.config.Config

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
object ConfigUtils {

  implicit class ConfigWrapper(config: Config) {
    implicit def getLongOpt(path: String): Option[Long] = getOpt(config.getLong(path))

    implicit def getStringOpt(path: String): Option[String] = getOpt(config.getString(path))

    implicit def getConfigOpt(path: String): Option[Config] = getOpt(config.getConfig(path))

    private[this] def getOpt[T](f: ⇒ T): Option[T] = {
      Try(f) match {
        case Success(value) ⇒ Some(value)
        case Failure(_)     ⇒ None
      }
    }
  }

}
