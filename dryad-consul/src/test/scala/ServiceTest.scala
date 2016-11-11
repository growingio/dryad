import io.growing.dryad.registry.ServiceProvider
import org.scalatest._

class ServiceTest extends FunSuite {

  test("Register service") {
    ServiceProvider().online()
  }

  test("Deregister service") {
    ServiceProvider().offline()
  }

}
