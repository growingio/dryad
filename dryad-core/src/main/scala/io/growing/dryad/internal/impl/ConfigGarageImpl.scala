package io.growing.dryad.internal.impl

import java.lang.reflect.Method
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import com.google.common.cache.{CacheBuilder, CacheLoader}
import io.growing.dryad.converter.ConfigConverters
import io.growing.dryad.internal.{ConfigGarage, ConfigService, Configuration}
import io.growing.dryad.reader.ConfigReader
import net.sf.cglib.proxy.{Enhancer, MethodInterceptor, MethodProxy}
import rx.functions.Action1
import rx.lang.scala.Observer

/**
 * Component:
 * Description:
 * Date: 16/3/31
 *
 * @author Andy Ai
 */
class ConfigGarageImpl(configService: ConfigService) extends ConfigGarage with Observer[Configuration] {

  configService.subject().subscribe(new Action1[Configuration] {
    override def call(t: Configuration): Unit = {
      onNext(t)
    }
  })

  private[this] val cache = CacheBuilder.newBuilder()
    .softValues()
    .expireAfterAccess(60, TimeUnit.MINUTES)
    .maximumSize(5000)
    .build(new CacheLoader[Class[_], AnyRef] {
      override def load(key: Class[_]): AnyRef = {
        createConfig(key)
      }
    })

  private[this] val classCache = CacheBuilder.newBuilder().softValues()
    .maximumSize(Int.MaxValue)
    .build[String, Class[_]]()

  override def get[T](clazz: Class[T]): T = {
    clazz.getAnnotations.find(_.isInstanceOf[io.growing.dryad.annotation.Configuration]) match {
      case None ⇒ throw new IllegalStateException(s"${clazz.getName} is not configuration")
      case Some(conf: io.growing.dryad.annotation.Configuration) ⇒ cache.get(clazz).asInstanceOf[T]
    }
  }

  override def onNext(value: Configuration): Unit = {
    val clazz = classCache.asMap().get(value.name)
    try {
      cache.get(clazz).asInstanceOf[AtomicReference[AnyRef]].set(createValue(clazz))
    } catch {
      case e: Throwable ⇒ e.printStackTrace()
    }
  }

  private[this] def createConfig[T](clazz: Class[T]): AnyRef = {
    val enhancer = new Enhancer()
    enhancer.setSuperclass(classOf[AtomicReference[T]])
    enhancer.setInterfaces(Array(clazz))
    enhancer.setCallbackType(classOf[Ref])
    enhancer.setCallback(new Ref)
    val config = enhancer.create()
    config.asInstanceOf[AtomicReference[T]].set(createValue(clazz).asInstanceOf[T])
    config
  }

  private[this] def createValue(clazz: Class[_]): AnyRef = {
    val anno = clazz.getAnnotation(classOf[io.growing.dryad.annotation.Configuration])
    val name: String = anno.value()
    classCache.put(name, clazz)
    val config = configService.get(name)
    val converter = extension(name) match {
      case "conf"       ⇒ ConfigConverters.conf
      case "properties" ⇒ ConfigConverters.properties
      case "yaml"       ⇒ ConfigConverters.yaml
      case extension    ⇒ throw new UnsupportedOperationException(s"Unsupported config extension $extension")
    }
    val source = converter.convert(config.payload)
    val reader = ConfigReader(source)
    val enhancer = new Enhancer()
    enhancer.setInterfaces(Array(clazz))
    enhancer.setCallbackType(classOf[Value])
    enhancer.setCallback(new Value(reader))
    enhancer.create()
  }

  private[this] def extension(name: String): String = {
    val index: Int = name.lastIndexOf('.')
    (if (index > 0 && index < name.length - 1) name.substring(index + 1) else name).toLowerCase
  }

}

private[this] class Value(reader: ConfigReader) extends MethodInterceptor {
  override def intercept(o: scala.Any, method: Method, objects: Array[AnyRef], methodProxy: MethodProxy): AnyRef = {
    val path = method.getName
    val t = method.getReturnType
    (t match {
      case _ if is(classOf[Int], t)         ⇒ reader.getInt(path)
      case _ if is(classOf[Long], t)        ⇒ reader.getLong(path)
      case _ if is(classOf[Double], t)      ⇒ reader.getDouble(path)
      case _ if is(classOf[String], t)      ⇒ reader.getString(path)
      case _ if is(classOf[Boolean], t)     ⇒ reader.getBoolean(path)
      case _ if is(classOf[Seq[String]], t) ⇒ Seq.empty[String]
      case _ if is(classOf[Seq[Int]], t)    ⇒ Seq.empty[Int]
      case _ if is(classOf[Seq[Long]], t)   ⇒ Seq.empty[Long]
      case _ if is(classOf[Seq[Double]], t) ⇒ Seq.empty[Double]
      case _                                ⇒ null
    }).asInstanceOf[AnyRef]
  }

  private[this] def is(clazz: Class[_], t: Class[_]): Boolean = clazz == t
}

private[this] class Ref extends MethodInterceptor {
  override def intercept(obj: scala.Any, method: Method, args: Array[AnyRef], proxy: MethodProxy): AnyRef = {
    val value = obj.asInstanceOf[AtomicReference[_]].get()
    method.invoke(value)
  }
}
