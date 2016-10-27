import Dependencies.Versions
import sbt.Keys._
import sbt._

object DryadBuild extends Build {

  lazy val commonSettings = Seq(
    version := "1.0.0-SNAPSHOT",
    organization := "io.growing",
    scalaVersion := Versions.scala
  )

  lazy val core = Project(id = "dryad-core", base = file("dryad-core"))
    .settings(mavenPublish: _*)
    .settings(commonSettings: _*)

  lazy val consul = Project(id = "dryad-consul", base = file("dryad-consul"))
    .settings(mavenPublish: _*)
    .settings(commonSettings: _*)
    .dependsOn(core)

  lazy val dryad = (project in file("."))
    .aggregate(core, consul)
    .settings(dontPublishSettings)
    .settings(
      aggregate in update := false
    )

  val dontPublishSettings = Seq(
    publish := (),
    publishArtifact in Compile := false
  )

  val mavenPublish = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo := {
      val nexus = "http://54.223.132.19:8081"
      if (version.value.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at nexus + "/nexus/content/repositories/snapshots/")
      } else {
        Some("releases" at nexus + "/nexus/content/repositories/releases/")
      }
    }
  )
}