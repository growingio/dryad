package io.growing.dryad

import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.config.{ Config, ConfigFactory }
import io.growing.dryad.parser.ConfigParser
import net.sf.cglib.proxy.{ Enhancer, MethodInterceptor, MethodProxy }
import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite

class AppTest extends AnyFunSuite {
  test("Default") {
    val dryadConfig = ConfigFactory.parseString(
      """
        dryad {
          namespace: default
          group: prod
          provider: io.growing.dryad.MemoryProvider
        }
      """.stripMargin)
    val configSystem = ConfigSystem(dryadConfig)
    val started = System.currentTimeMillis()
    val devConfig = configSystem.get[DevConfig]
    println(s"Cost: ${System.currentTimeMillis() - started}")
    println(devConfig.toString)
    assert("Shanghai" == devConfig.addr.city)
    Thread.sleep(3000)
    assert("Beijing" == devConfig.addr.city)
    println(devConfig.toString)
  }

  test("CGlib") {
    val config = DevConfig(18, "Andy", Addr("Shanghai"))
    val clazz = classOf[DevConfig]
    val enhancer = new Enhancer()
    enhancer.setSuperclass(clazz)
    enhancer.setCallbackType(classOf[Ref])
    val reference = new AtomicReference[AnyRef](config)
    val ref: Ref = new Ref(reference)
    enhancer.setCallback(ref)
    val parameterTypes: Array[Class[_]] = clazz.getConstructors.head.getParameterTypes
    val obj = enhancer.create(parameterTypes, parameterTypes.map { c ⇒
      val x = c.getName match {
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
      x.asInstanceOf[AnyRef]
    })
    val x = obj.asInstanceOf[DevConfig]
    assert("Shanghai" == x.addr.city)
    ref.reference.set(DevConfig(18, "Andy", Addr("Beijing")))
    assert("Beijing" == x.addr.city)
  }

}

class DefConfigParser extends ConfigParser[DevConfig] {
  override def parse(config: Config): DevConfig = {
    DevConfig(config.getInt("age"), config.getString("name"), Addr(config.getString("addr.city")))
  }
}

final case class Addr(city: String)

@annotation.Configuration(name = "application.conf", parser = classOf[DefConfigParser])
case class DevConfig(age: Int, name: String, addr: Addr)

class Ref(val reference: AtomicReference[AnyRef]) extends MethodInterceptor {

  override def intercept(o: scala.Any, method: Method, objects: Array[AnyRef], methodProxy: MethodProxy): AnyRef = {
    method.invoke(reference.get)
  }
}

