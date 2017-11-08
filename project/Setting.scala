import Dependencies.Versions
import sbt.Keys._
import sbt.{AutoPlugin, PluginTrigger}

object Setting extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
    version := "1.0.4",
    organization := "io.growing",
    scalaVersion := Versions.scala212,
    crossScalaVersions := Seq(Versions.scala212)
  )

}
