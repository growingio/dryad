package io.growing.dryad.consul

import com.typesafe.config.Config
import io.growing.dryad.annotation.Configuration
import io.growing.dryad.parser.ConfigParser

import scala.collection.JavaConverters._

/**
 * Component:
 * Description:
 * Date: 16/3/17
 *
 * @author Andy Ai
 */
@Configuration(name = "application.conf", parser = classOf[ApplicationConfigParser2], ignoreGroup = true)
case class ApplicationConfig2(name: String, age: Int, cars: Option[Seq[String]])

class ApplicationConfigParser2 extends ConfigParser[ApplicationConfig2] {
  override def parse(config: Config): ApplicationConfig2 = {
    val cars = if (config.hasPath("cars")) {
      Option(config.getStringList("cars").asScala)
    } else {
      None
    }
    ApplicationConfig2(config.getString("name"), config.getInt("age"), cars)
  }
}
