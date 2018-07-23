package io.growing.dryad

import io.growing.dryad.internal.ConfigurationDesc
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.watcher.ConfigChangeListener

/**
 * Component:
 * Description:
 * Date: 2018/7/23
 *
 * @author AI
 */
class MemoryProvider extends ConfigProvider {

  override def load(path: String): ConfigurationDesc = {
    load(path, null)
  }

  override def load(path: String, listener: ConfigChangeListener): ConfigurationDesc = {
    val thread = new Thread() {
      override def run(): Unit = {
        Thread.sleep(2000)
        listener.onChange(
          ConfigurationDesc(
            path,
            """
              age: 18
              name: Andy
              addr: {
                city: Beijing
              }
            """.stripMargin, 1))
      }
    }
    thread.start()
    ConfigurationDesc(
      path,
      """
        age: 18
        name: Andy
        addr: {
          city: Shanghai
        }
      """.stripMargin, 0)
  }

}
