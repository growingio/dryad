name := "dryad"

enablePlugins(DontPublish, Setting)

lazy val core = Project(id = "dryad-core", base = file("dryad-core"))
  .enablePlugins(Publish)
  .enablePlugins(Setting)

lazy val consul = Project(id = "dryad-consul", base = file("dryad-consul"))
  .enablePlugins(Publish)
  .enablePlugins(Setting)
  .dependsOn(core)
