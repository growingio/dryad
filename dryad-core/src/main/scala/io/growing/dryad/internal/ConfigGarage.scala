package io.growing.dryad.internal

/**
 * Component:
 * Description:
 * Date: 16/3/31
 *
 * @author Andy Ai
 */
trait ConfigGarage {

  def get[T](clazz: Class[T]): T

}
