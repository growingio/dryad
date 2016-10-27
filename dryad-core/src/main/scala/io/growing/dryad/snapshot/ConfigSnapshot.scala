package io.growing.dryad.snapshot

import java.nio.file.{FileSystems, Files, Paths, StandardOpenOption}

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
  val separator = FileSystems.getDefault.getSeparator
  val workshop = {
    val name = s"${System.getProperty("user.home")}$separator.dryad${separator}snapshots"
    val path = Paths.get(name)
    if (!Files.exists(path)) {
      Files.createDirectories(path)
    }
    name
  }

  override def flash(configuration: ConfigurationDesc): Unit = {
    val dir = Paths.get(Array(workshop, configuration.namespace, configuration.group).filter(_.trim.nonEmpty).mkString(separator))
    if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    }
    val path = dir.resolve(s"${configuration.name}-${configuration.version}")
    if (!Files.exists(path)) {
      Files.write(path, configuration.payload.getBytes(Charsets.UTF_8), StandardOpenOption.CREATE_NEW)
    }
  }

}