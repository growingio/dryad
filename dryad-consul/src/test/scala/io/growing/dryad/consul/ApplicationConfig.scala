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
@Configuration(name = "application.conf", parser = classOf[ApplicationConfigParser])
case class ApplicationConfig(name: String, age: Int, cars: Option[Seq[String]])

class ApplicationConfigParser extends ConfigParser[ApplicationConfig] {
  override def parse(config: Config): ApplicationConfig = {
    val cars = if (config.hasPath("cars")) {
      Option(config.getStringList("cars").asScala)
    } else {
      None
    }
    ApplicationConfig(config.getString("name"), config.getInt("age"), cars)
  }
}
