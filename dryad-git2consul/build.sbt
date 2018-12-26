import Dependencies.Versions
//import xerial.sbt.pack.PackPlugin._

crossScalaVersions := Seq(Versions.scalaLibrary)

enablePlugins(Setting, PackPlugin)

libraryDependencies ++= Dependencies.git2Consul
