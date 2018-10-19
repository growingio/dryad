import sbt._

object Dependencies {

  object Versions {
    val config = "1.3.3"
    val scala = "2.12.7"
    val jersey = "2.22.2"
    val configs = "0.4.4"
    val guava = "27.0-jre"
    val scalatest = "3.0.5"
    val cglibNodep = "3.2.8"
    val scalaLogging = "3.9.0"
    val consulClient = "1.2.6"
  }

  object Compiles {
    val config: ModuleID = "com.typesafe" % "config" % Versions.config
    val cglib: ModuleID = "cglib" % "cglib-nodep" % Versions.cglibNodep
    val guava: ModuleID = "com.google.guava" % "guava" % Versions.guava
    val configs: ModuleID = "com.github.kxbmap" %% "configs" % Versions.configs
    val consul: ModuleID = "com.orbitz.consul" % "consul-client" % Versions.consulClient
    val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Dependencies.Compiles._

  val dryadCore: Seq[ModuleID] = Seq(configs, cglib, logging, config, guava, Tests.scalaTest)

  val dryadConsul: Seq[ModuleID] = Seq(consul, Tests.scalaTest)

  val dryadCluster: Seq[ModuleID] = Seq(Tests.scalaTest)

}
