import Dependencies.Versions
import xerial.sbt.pack.PackPlugin._

organization := "io.growing"

scalaVersion := Versions.scalaLibrary

dependencyUpgradeModuleNames := Map(
  "log4j.*" -> "log4j2",
  "scala-library" -> "scala")

crossScalaVersions := Seq(Versions.scalaLibrary)

enablePlugins(Setting, PackPlugin)

libraryDependencies ++= Dependencies.git2Consul

packMain := Map("git2consul" -> "io.growing.dryad.git2consul.Git2ConsulBootstrap")

packExtraClasspath := Map("git2consul" -> Seq("${PROG_HOME}/conf"))

publishPackArchives

