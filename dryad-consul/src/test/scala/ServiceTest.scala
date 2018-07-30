import io.growing.dryad.ServiceProvider
import org.scalatest._

class ServiceTest extends FunSuite {

  private val provider = ServiceProvider()

  test("Register service") {
    provider.register()
  }

  test("Deregister service") {
    provider.deregister()
  }

}
