import Dependencies.Versions
import org.jmotor.sbt.plugin.DependencyUpdatesPlugin.autoImport._
import sbt.Keys._
import sbt.{ AutoPlugin, PluginTrigger }

object Setting extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
    organization := "io.growing",
    scalaVersion := Versions.scalaLibrary,
    crossScalaVersions := Seq(Versions.scalaLibrary, Versions.scala211),
    dependencyUpgradeModuleNames := Map(
      "log4j.*" -> "log4j2",
      "scala-library" -> "scala"))

}
