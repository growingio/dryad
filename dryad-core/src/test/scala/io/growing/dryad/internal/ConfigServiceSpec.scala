package io.growing.dryad.internal

import java.nio.file.Files

import io.growing.dryad.internal.impl.ConfigServiceImpl
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.watcher.ConfigChangeListener
import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018-12-27
 *
 * @author AI
 */
class ConfigServiceSpec extends FunSuite {

  test("download") {
    val configService = new ConfigServiceImpl(new ConfigProvider {
      override def load(path: String): ConfigurationDesc = {
        ConfigurationDesc(
          path,
          """
            |{
            |  name = "Andy Ai"
            |}
          """.stripMargin, 0)
      }

      override def load(path: String, listener: ConfigChangeListener): ConfigurationDesc = ???
    })

    val root = Files.createTempDirectory("dryad-download")
    val path = "/_global_/application.conf"
    val file = configService.download(root, path)
    assert(Files.exists(file))
  }

}
