import Dependencies.Versions
import xerial.sbt.pack.PackPlugin.autoImport.packExtraClasspath
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

lazy val utf8: String = "UTF-8"
lazy val javaVersion: String = "1.8"

lazy val root = Project(id = "dryad", base = file("."))
  .settings(
    organization := "io.growing",
    scalaVersion := Versions.scala212,
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
    crossScalaVersions := Seq(Dependencies.Versions.scala212),
    packMain := Map("git2consul" -> "io.growing.dryad.git2consul.Git2ConsulBootstrap"),
    packExtraClasspath := Map("git2consul" -> Seq("${PROG_HOME}/conf"))
  )
  .enablePlugins(PackPlugin, DontPublish)

def dryadModule(name: String): Project = Project(id = name, base = file(name))
  .settings(
    organization := "io.growing",
    scalaVersion := Versions.scala212,
    dependencyUpgradeModuleNames := Map(
      "log4j.*" -> "log4j2",
      "scala-library" -> "scala"),
    Compile / compile / javacOptions ++= Seq("-source", javaVersion, "-target", javaVersion, "-encoding", utf8, "-deprecation")
  )

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
