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
    .settings(commonSettings: _*)

  lazy val consul = Project(id = "dryad-consul", base = file("dryad-consul"))
    .settings(commonSettings: _*)
    .dependsOn(core)

  lazy val dryad = (project in file("."))
    .aggregate(core, consul)
    .settings(
      aggregate in update := false
    )
}