package io.growing.dryad.converter

import java.io.ByteArrayInputStream
import java.util.Properties

import com.google.common.base.Charsets
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Component:
 * Description:
 * Date: 16/3/31
 *
 * @author Andy Ai
 */
trait ConfigConverter[T] {
  def convert(payload: String): T
}

object ConfigConverters {

  val properties = new ConfigConverter[Properties] {
    override def convert(payload: String): Properties = {
      val _properties = new Properties()
      val inputStream = new ByteArrayInputStream(payload.getBytes(Charsets.UTF_8))
      try {
        _properties.load(inputStream)
      } finally {
        if (null != inputStream) {
          inputStream.close()
        }
      }
      _properties
    }
  }

  val conf = new ConfigConverter[Config] {
    override def convert(payload: String): Config = {
      ConfigFactory.parseString(payload)
    }
  }

}