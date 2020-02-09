package io.growing.dryad.concurrent

import java.util.Timer

import com.typesafe.scalalogging.LazyLogging

import scala.util.control.NonFatal

/**
 * Copied from https://github.com/Netflix/netflix-commons/blob/master/netflix-commons-util/src/main/java/com/netflix/util/concurrent/ShutdownEnabledTimer.java
 *
 * @author AI
 *         2020/2/9
 */
class ShutdownEnabledTimer(name: String, daemon: Boolean) extends Timer(name, daemon) with LazyLogging {

  private[this] val cancelThread = new Thread() {
    override def run(): Unit = {
      ShutdownEnabledTimer.super.cancel()
    }
  }

  logger.info("Shutdown hook installed for: {}", name)

  Runtime.getRuntime.addShutdownHook(cancelThread)

  override def cancel(): Unit = {
    super.cancel()
    logger.info("Shutdown hook removed for: {}", name)
    try {
      Runtime.getRuntime.removeShutdownHook(cancelThread)
    } catch {
      case NonFatal(t) ⇒ logger.info("Exception caught (might be ok if at shutdown)", t)
    }
  }

}
