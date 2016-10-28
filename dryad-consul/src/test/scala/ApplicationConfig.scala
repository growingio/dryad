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
@Configuration(name = "application.conf", parser = classOf[ApplicationConfigParser])
case class ApplicationConfig(name: String, age: Int, cars: Option[Seq[String]])

class ApplicationConfigParser extends ConfigParser[ApplicationConfig] {
  override def parse(config: Config): ApplicationConfig = Configs[ApplicationConfig].extract(config).value
}
