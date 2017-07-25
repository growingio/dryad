import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.config.{ Config, ConfigFactory }
import configs.Configs
import io.growing.dryad.internal.ConfigurationDesc
import io.growing.dryad.parser.ConfigParser
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.watcher.ConfigChangeListener
import io.growing.dryad.{ ConfigSystem, annotation }
import net.sf.cglib.proxy.{ Enhancer, MethodInterceptor, MethodProxy }
import org.scalatest._

class AppTest extends FunSuite {
  test("Default") {
    val dryadConfig = ConfigFactory.parseString(
      """
        dryad {
          namespace: default
          group: prod
          provider: MemoryProvider
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

class MemoryProvider extends ConfigProvider {

  override def load(name: String, namespace: String, group: Option[String], listener: ConfigChangeListener): ConfigurationDesc = {
    val thread = new Thread() {
      override def run(): Unit = {
        Thread.sleep(2000)
        listener.onChange(
          ConfigurationDesc(
            name,
            """
              age: 18
              name: Andy
              addr: {
                city: Beijing
              }
            """.stripMargin, 1, namespace, group))
      }
    }
    thread.start()
    ConfigurationDesc(
      name,
      """
        age: 18
        name: Andy
        addr: {
          city: Shanghai
        }
      """.stripMargin, 0, namespace, group)
  }

}

class DefConfigParser extends ConfigParser[DevConfig] {
  override def parse(config: Config): DevConfig = Configs[DevConfig].extract(config).value
}

case class Addr(city: String)

@annotation.Configuration(name = "application.conf", parser = classOf[DefConfigParser])
case class DevConfig(age: Int, name: String, addr: Addr)

class Ref(val reference: AtomicReference[AnyRef]) extends MethodInterceptor {

  override def intercept(o: scala.Any, method: Method, objects: Array[AnyRef], methodProxy: MethodProxy): AnyRef = {
    method.invoke(reference.get)
  }
}

