package io.growing.dryad.util

import com.typesafe.config.Config

import scala.language.implicitConversions
import scala.util.{ Failure, Success, Try }
import scala.collection.JavaConverters._

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

    implicit def getStringSeqOpt(path: String): Option[Seq[String]] = getOpt(config.getStringList(path).asScala)

    private[this] def getOpt[T](f: ⇒ T): Option[T] = {
      Try(f) match {
        case Success(value) ⇒ Some(value)
        case Failure(_)     ⇒ None
      }
    }
  }

}
