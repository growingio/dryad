import Dependencies.Versions
import ReleaseTransformations._

name := "dryad"

enablePlugins(DontPublish, Setting)

lazy val core = Project(id = "dryad-core", base = file("dryad-core"))
  .enablePlugins(Publish)
  .enablePlugins(Setting)

lazy val consul = Project(id = "dryad-consul", base = file("dryad-consul"))
  .enablePlugins(Publish)
  .enablePlugins(Setting)
  .dependsOn(core)

lazy val cluster = Project(id = "dryad-cluster", base = file("dryad-cluster"))
  .enablePlugins(Publish)
  .enablePlugins(Setting)
  .dependsOn(core)

lazy val git2Consul = Project(id = "dryad-git2consul", base = file("dryad-git2consul"))
  .enablePlugins(Setting)
  .enablePlugins(DontPublish)
  .settings(crossScalaVersions := Seq(Versions.scalaLibrary))

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

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
