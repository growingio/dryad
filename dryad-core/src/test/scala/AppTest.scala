import java.lang

import io.growing.dryad.annotation.Configuration
import org.scalatest._

class AppTest extends FlatSpec with Matchers {
  "Default test" should "Print something" in {

  }

  private def newInstance[T](clazz: Class[T]): T = {
    val constructor = clazz.getConstructors.head
    val types: Array[Class[_]] = constructor.getParameterTypes
    val values = types.map(init)
    constructor.newInstance(values: _*).asInstanceOf[T]
  }

  private def init(clazz: Class[_]): Object = clazz.getName match {
    case "int" ⇒ new java.lang.Integer(0)
    case "long" ⇒ new lang.Long(0)
    case "scala.Option" ⇒ scala.Option.empty
    case name if name.startsWith("scala.collection") ⇒ scala.collection.Iterable()
    case _ ⇒ clazz.newInstance().asInstanceOf[Object]
  }
}
