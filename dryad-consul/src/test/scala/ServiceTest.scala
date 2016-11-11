import io.growing.dryad.registry.Servlet
import org.scalatest._

@Ignore class ServiceTest extends FunSuite {

  test("Register service") {
    Servlet().online()
  }

  test("Deregister service") {
    Servlet().offline()
  }

}
