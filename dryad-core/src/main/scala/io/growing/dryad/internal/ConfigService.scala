package io.growing.dryad.internal

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

  def get[T: ClassTag](namespace: String, group: String): T

}

object ConfigService {

  def apply(provider: ConfigProvider): ConfigService = new ConfigServiceImpl(provider)

}
