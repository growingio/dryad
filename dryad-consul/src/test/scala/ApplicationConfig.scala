import io.growing.dryad.annotation.Configuration

/**
 * Component:
 * Description:
 * Date: 16/3/17
 *
 * @author Andy Ai
 */
@Configuration("application.conf")
trait ApplicationConfig {
  val name: String
  val age: Int
  val cars: Seq[String]
}
