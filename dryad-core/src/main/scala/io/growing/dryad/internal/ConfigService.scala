package io.growing.dryad.internal

import java.nio.file.Path

import com.typesafe.config.Config
import io.growing.dryad.internal.impl.ConfigServiceImpl
import io.growing.dryad.provider.ConfigProvider

import scala.reflect.ClassTag

/**
 * Component:
 * Description:
 * Date: 16/3/25
 *
 * @author Andy Ai
 */
trait ConfigService {

  def download(root: Path, path: String): Path

  def get[T: ClassTag](namespace: String, group: String): T

  def get(name: String, namespace: String, group: Option[String]): Config

  def getConfigAsString(name: String, namespace: String, group: Option[String]): String

  def getConfigAsStringRecursive(path: String, namespace: String, group: String): String

}

object ConfigService {

  def apply(provider: ConfigProvider): ConfigService = new ConfigServiceImpl(provider)

}
