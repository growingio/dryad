import org.scalatest._
import org.yaml.snakeyaml.Yaml

import scala.collection.mutable

class YAMLTest extends FlatSpec with Matchers {
  "Default test" should "Print something" in {
    val yaml = new Yaml()
    val x = yaml.loadAs(getClass.getClassLoader.getResourceAsStream("application.yaml"), classOf[mutable.LinkedHashMap[String, String]])
    println(yaml.getName)
    println(x)
  }

}
