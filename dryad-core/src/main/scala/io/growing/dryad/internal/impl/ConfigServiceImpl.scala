package io.growing.dryad.internal.impl

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicReference

import com.google.common.cache.CacheBuilder
import com.typesafe.config.{Config, ConfigFactory, ConfigRef}
import io.growing.dryad.annotation.Configuration
import io.growing.dryad.internal.{ConfigService, ConfigurationDesc}
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.snapshot.LocalFileConfigSnapshot
import io.growing.dryad.watcher.ConfigChangeListener
import net.sf.cglib.proxy.Enhancer

import scala.reflect.ClassTag

/**
 * Component:
 * Description:
 * Date: 16/3/26
 *
 * @author Andy Ai
 */
class ConfigServiceImpl(provider: ConfigProvider) extends ConfigService {

  private[this] val objects = CacheBuilder.newBuilder().build[String, AnyRef]()
  private[this] val configs = CacheBuilder.newBuilder().build[String, Config]()

  override def get[T: ClassTag](namespace: String, group: String): T = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    objects.get(clazz.getName, new Callable[AnyRef] {
      override def call(): AnyRef = createObjectRef(clazz, namespace, group)
    }).asInstanceOf[T]
  }

  override def get(namespace: String, group: String, name: String): Config = {
    configs.get(name, new Callable[Config] {
      override def call(): Config = {
        val underlying: AtomicReference[Config] = new AtomicReference[Config]()
        val c = provider.load(name, namespace, group, new ConfigChangeListener {
          override def onChange(configuration: ConfigurationDesc): Unit = {
            val config = ConfigFactory.parseString(configuration.payload)
            underlying.set(config)
            LocalFileConfigSnapshot.flash(configuration)
          }
        })
        LocalFileConfigSnapshot.flash(c)
        underlying.set(ConfigFactory.parseString(c.payload))
        new ConfigRef(underlying)
      }
    })
  }

  private[this] def createObjectRef(clazz: Class[_], namespace: String, group: String): AnyRef = {
    val annotation = clazz.getAnnotation(classOf[Configuration])
    val ref: ObjectRef = new ObjectRef(new AtomicReference[Any]())
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
    enhancer.setCallbackType(classOf[ObjectRef])
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

