import org.scalatest._
import org.yaml.snakeyaml.Yaml

class YAMLTest extends FlatSpec with Matchers {
  "Default test" should "Print something" in {
    val yaml = new Yaml()
    val x = yaml.load(getClass.getClassLoader.getResourceAsStream("application.yaml"))
    println(yaml.getName)
    println(x)
  }

}
