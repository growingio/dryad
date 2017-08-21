import Dependencies.Versions
import sbt.{AutoPlugin, PluginTrigger}
import sbt.Keys._
import sbt._

object Setting extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
    version := "1.0.3",
    organization := "io.growing",
    scalaVersion := Versions.scala212,
    scalacOptions ++= byScalaVersion {
      case (2, 12) => Seq("-opt:l:method")
      case (2, 11) => Seq("-Xexperimental")
    }.value,
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212)
  )

  def byScalaVersion[A](f: PartialFunction[(Int, Int), Seq[A]]): Def.Initialize[Seq[A]] =
    Def.setting {
      CrossVersion.partialVersion(scalaVersion.value)
        .flatMap(f.lift)
        .getOrElse(Seq.empty)
    }
}
