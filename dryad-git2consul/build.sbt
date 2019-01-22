import xerial.sbt.pack.PackPlugin._

enablePlugins(Setting, PackPlugin)

libraryDependencies ++= Dependencies.git2Consul

packMain := Map("git2consul" -> "io.growing.dryad.git2consul.Git2ConsulBootstrap")

packExtraClasspath := Map("git2consul" -> Seq("${PROG_HOME}/conf"))

publishPackArchives
