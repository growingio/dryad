package io.growing.dryad.portal

/**
 * Component:
 * Description:
 * Date: 2018/7/16
 *
 * @author AI
 */
object Schema extends Enumeration {

  type Schema = Value

  val HTTP: Value = Value("http")
  val GRPC: Value = Value("grpc")
  val WEB_SOCKET: Value = Value("web-socket")

}
