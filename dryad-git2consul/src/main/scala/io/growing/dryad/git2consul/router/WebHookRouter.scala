package io.growing.dryad.git2consul.router

import com.google.common.reflect.ClassPath.ClassInfo
import com.google.common.reflect.{ ClassPath, TypeToken }
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.hook.ConfigurationHook
import io.undertow.server.{ HttpHandler, HttpServerExchange }
import io.undertow.util.{ Methods, StatusCodes }
import org.jmotor.http.RoutingHandler

import scala.collection.JavaConverters._

/**
 * Component:
 * Description:
 * Date: 2018-12-24
 *
 * @author AI
 */
class WebHookRouter(config: Git2ConsulConfig) extends HttpHandler with RoutingHandler {

  private[this] val hooks: Map[String, ConfigurationHook] = ClassPath.from(this.getClass.getClassLoader)
    .getTopLevelClasses("io.growing.dryad.git2consul.hook.impl").asScala.collect {
      case classInfo: ClassInfo if TypeToken.of(classInfo.load()).isSubtypeOf(classOf[ConfigurationHook]) ⇒
        val constructor = classInfo.load().getConstructor(classOf[Git2ConsulConfig])
        val hook = constructor.newInstance(config).asInstanceOf[ConfigurationHook]
        hook.name -> hook
    }.toMap

  override def route: String = "/git2consul/hooks/{name}"

  override def methods: Set[String] = single(Methods.POST_STRING)

  override def handleRequest(exchange: HttpServerExchange): Unit = {
    if (exchange.isInIoThread) {
      exchange.dispatch(this)
    } else {
      val name = exchange.getQueryParameters.get("name").getFirst
      hooks.get(name) match {
        case None ⇒
          exchange.setStatusCode(StatusCodes.NOT_FOUND)
          exchange.endExchange()
        case Some(hook) ⇒ hook.call(exchange)
      }
    }
  }

}
