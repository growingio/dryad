package io.growing.dryad.git2consul.concurrent

import java.util.concurrent.Executors
import scala.concurrent.{ ExecutionContext ⇒ SExecutionContext }

/**
 * Component:
 * Description:
 * Date: 2018-12-26
 *
 * @author AI
 */
object ExecutionContext {

  def io: SExecutionContext = SExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() * 2))

  object Implicits {
    implicit lazy final val IO: SExecutionContext = io
  }

}
