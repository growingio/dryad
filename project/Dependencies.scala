import sbt._

object Dependencies {

  object Versions {
    val cglib = "3.2.5"
    val jersey = "2.22.2"
    val config = "1.3.2"
    val consul = "0.17.0"
    val configs = "0.4.4"
    val logging = "3.7.2"
    val guava = "23.3-jre"
    val scala212 = "2.12.4"
    val scalaTest = "3.0.4"
  }

  object Compile {
    val cglib: ModuleID = "cglib" % "cglib-nodep" % Versions.cglib
    val config: ModuleID = "com.typesafe" % "config" % Versions.config
    val guava: ModuleID = "com.google.guava" % "guava" % Versions.guava
    val configs: ModuleID = "com.github.kxbmap" %% "configs" % Versions.configs
    val consul: ModuleID = "com.orbitz.consul" % "consul-client" % Versions.consul
    val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Versions.logging
  }

  object Test {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }

  import Dependencies.Compile._

  val dryadCore: Seq[ModuleID] = Seq(configs, cglib, logging, config, guava, Test.scalaTest)

  val dryadConsul: Seq[ModuleID] = Seq(consul, Test.scalaTest)
}
