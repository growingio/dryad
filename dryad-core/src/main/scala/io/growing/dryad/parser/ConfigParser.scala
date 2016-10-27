package io.growing.dryad.parser

import com.typesafe.config.Config

/**
 * Component:
 * Description:
 * Date: 16/3/31
 *
 * @author Andy Ai
 */
trait ConfigParser[T] {

  def parse(config: Config): T

}
