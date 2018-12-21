package io.growing.dryad.consul

import com.typesafe.config.Config
import configs.Configs
import io.growing.dryad.annotation.Configuration
import io.growing.dryad.parser.ConfigParser

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
  override def parse(config: Config): ApplicationConfig2 = Configs[ApplicationConfig2].extract(config).value
}
