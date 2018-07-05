import sbt._

object Dependencies {

  object Versions {
    val config = "1.3.3"
    val scala = "2.12.6"
    val jersey = "2.22.2"
    val configs = "0.4.4"
    val guava = "25.1-jre"
    val scalatest = "3.0.5"
    val cglibNodep = "3.2.7"
    val scalaLogging = "3.9.0"
    val consulClient = "1.2.1"
  }

  object Compile {
    val config: ModuleID = "com.typesafe" % "config" % Versions.config
    val cglib: ModuleID = "cglib" % "cglib-nodep" % Versions.cglibNodep
    val guava: ModuleID = "com.google.guava" % "guava" % Versions.guava
    val configs: ModuleID = "com.github.kxbmap" %% "configs" % Versions.configs
    val consul: ModuleID = "com.orbitz.consul" % "consul-client" % Versions.consulClient
    val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
  }

  object Test {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
  }

  import Dependencies.Compile._

  val dryadCore: Seq[ModuleID] = Seq(configs, cglib, logging, config, guava, Test.scalaTest)

  val dryadConsul: Seq[ModuleID] = Seq(consul, Test.scalaTest)
}
