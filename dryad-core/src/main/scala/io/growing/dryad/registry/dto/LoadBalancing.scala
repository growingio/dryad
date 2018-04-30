package io.growing.dryad.registry.dto

/**
 * Component:
 * Description:
 * Date: 2018/4/30
 *
 * @author AI
 */
object LoadBalancing extends Enumeration {

  type LoadBalancing = Value

  val UrlHash: Value = Value("url_chash")
  val RoundRobin: Value = Value("round_robin")

}
