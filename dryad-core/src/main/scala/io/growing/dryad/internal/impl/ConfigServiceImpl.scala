package io.growing.dryad.internal.impl

import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicReference

import com.google.common.cache.CacheBuilder
import com.typesafe.config.ConfigFactory
import io.growing.dryad.annotation.Configuration
import io.growing.dryad.internal.{ConfigService, ConfigurationDesc}
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.snapshot.LocalFileConfigSnapshot
import io.growing.dryad.watcher.ConfigChangeListener
import net.sf.cglib.proxy.{Enhancer, MethodInterceptor, MethodProxy}

import scala.reflect.ClassTag

/**
 * Component:
 * Description:
 * Date: 16/3/26
 *
 * @author Andy Ai
 */
class ConfigServiceImpl(provider: ConfigProvider) extends ConfigService {

  private[this] val caches = CacheBuilder.newBuilder().build[String, AnyRef]()

  override def get[T: ClassTag](namespace: String, group: String): T = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    caches.get(clazz.getName, new Callable[AnyRef] {
      override def call(): AnyRef = createRef(clazz, namespace, group)
    }).asInstanceOf[T]
  }

  private[this] def createRef(clazz: Class[_], namespace: String, group: String): AnyRef = {
    val annotation = clazz.getAnnotation(classOf[Configuration])
    val ref: Ref = new Ref(new AtomicReference[Any]())
    val parser = annotation.parser().newInstance()
    val configuration = provider.load(annotation.name(), namespace, group, new ConfigChangeListener {

      override def onChange(configuration: ConfigurationDesc): Unit = {
        val config = ConfigFactory.parseString(configuration.payload)
        ref.reference.set(parser.parse(config))
        LocalFileConfigSnapshot.flash(configuration)
      }

    })
    LocalFileConfigSnapshot.flash(configuration)
    val config = ConfigFactory.parseString(configuration.payload)
    ref.reference.set(parser.parse(config))

    val enhancer = new Enhancer()
    enhancer.setSuperclass(clazz)
    enhancer.setCallbackType(classOf[Ref])
    enhancer.setCallback(ref)
    val parameterTypes: Array[Class[_]] = clazz.getConstructors.head.getParameterTypes
    val refObj = enhancer.create(parameterTypes, parameterTypes.map { c ⇒
      val v = c.getName match {
        case "byte"    ⇒ 0
        case "short"   ⇒ 0
        case "int"     ⇒ 0
        case "long"    ⇒ 0L
        case "float"   ⇒ 0.0f
        case "double"  ⇒ 0.0d
        case "char"    ⇒ '\u0000'
        case "boolean" ⇒ false
        case _         ⇒ null
      }
      v.asInstanceOf[AnyRef]
    })
    refObj
  }

}

private[impl] class Ref(val reference: AtomicReference[Any]) extends MethodInterceptor {

  override def intercept(o: scala.Any, method: Method, objects: Array[AnyRef], methodProxy: MethodProxy): AnyRef = {
    method.invoke(reference.get())
  }

}
