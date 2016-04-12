package io.growing.dryad.parser

import io.growing.dryad.reader.ConfigReader

/**
 * Component:
 * Description:
 * Date: 16/3/31
 *
 * @author Andy Ai
 */
trait ConfigParser[T] {

  def parse(clazz: Class[T], reader: ConfigReader): T

}
