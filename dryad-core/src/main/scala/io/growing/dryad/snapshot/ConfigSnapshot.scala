package io.growing.dryad.snapshot

import java.nio.file.{ FileSystems, Files, Paths, StandardOpenOption }

import com.google.common.base.Charsets
import io.growing.dryad.internal.ConfigurationDesc

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
trait ConfigSnapshot {

  def flash(configuration: ConfigurationDesc): Unit

}

object LocalFileConfigSnapshot extends ConfigSnapshot {
  val fileSeparator: String = FileSystems.getDefault.getSeparator
  val workshop: String = {
    val name = s"${System.getProperty("user.home")}$fileSeparator.dryad${fileSeparator}snapshots"
    val path = Paths.get(name)
    if (!Files.exists(path)) {
      Files.createDirectories(path)
    }
    name
  }

  override def flash(configuration: ConfigurationDesc): Unit = {
    val segments = configuration.path.split("/")
    val name = segments.last
    val dir = Paths.get(segments.dropRight(1).mkString(fileSeparator))
    if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    }
    val path = dir.resolve(s"$name-${configuration.version}")
    if (!Files.exists(path)) {
      Files.write(path, configuration.payload.getBytes(Charsets.UTF_8), StandardOpenOption.CREATE)
    }
  }

}
