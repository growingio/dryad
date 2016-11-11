import Dependencies.Versions
import sbt.{AutoPlugin, PluginTrigger}
import sbt.Keys._

object Setting extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
    version := "1.0.0-SNAPSHOT",
    organization := "io.growing",
    scalaVersion := Versions.scala212,
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212)
  )

}