import Dependencies.Versions
import xerial.sbt.pack.PackPlugin.autoImport.packExtraClasspath

lazy val root = Project(id = "dryad", base = file("."))
  .settings(
    organization := "io.growing",
    scalaVersion := Versions.scalaLibrary,
  )
  .enablePlugins(DontPublish)
  .aggregate(core, consul, cluster, git2Consul)

lazy val core = dryadModule("dryad-core")
  .settings(Dependencies.dryadCore)
  .settings(crossScalaVersions := Dependencies.crossScalaVersions)

lazy val consul = dryadModule("dryad-consul")
  .settings(Dependencies.dryadConsul)
  .settings(crossScalaVersions := Dependencies.crossScalaVersions)
  .dependsOn(core)

lazy val cluster = dryadModule("dryad-cluster")
  .settings(Dependencies.dryadCluster)
  .settings(crossScalaVersions := Dependencies.crossScalaVersions)
  .dependsOn(core)

lazy val git2Consul = dryadModule("dryad-git2consul")
  .settings(Dependencies.git2Consul)
  .settings(
    crossScalaVersions := Seq(Dependencies.Versions.scalaLibrary),
    packMain := Map("git2consul" -> "io.growing.dryad.git2consul.Git2ConsulBootstrap"),
    packExtraClasspath := Map("git2consul" -> Seq("${PROG_HOME}/conf"))
  )
  .enablePlugins(PackPlugin, DontPublish)

def dryadModule(name: String): Project = Project(id = name, base = file(name))
  .settings(
    organization := "io.growing",
    scalaVersion := Versions.scalaLibrary,
    dependencyUpgradeModuleNames := Map(
      "log4j.*" -> "log4j2",
      "scala-library" -> "scala")
  )
