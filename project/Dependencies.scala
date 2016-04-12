import sbt._

object Dependencies {

  object Versions {
    val guava = "19.0"
    val cglib = "3.2.2"
    val scala = "2.11.7"
    val jersey = "2.22.2"
    val config = "1.3.0"
    val consul = "0.10.1"
    val rxScala = "0.26.0"
    val logging = "3.1.0"
    val snakeYAML = "1.17"
    val scalaTest = "2.2.6"
  }

  object Compile {
    val cglib = "cglib" % "cglib-nodep" % Versions.cglib
    val config = "com.typesafe" % "config" % Versions.config
    val guava = "com.google.guava" % "guava" % Versions.guava
    val rxScala = "io.reactivex" %% "rxscala" % Versions.rxScala
    val snakeYAML = "org.yaml" % "snakeyaml" % Versions.snakeYAML
    val consul = "com.orbitz.consul" % "consul-client" % Versions.consul
    val logging = "com.typesafe.scala-logging" %% "scala-logging" % Versions.logging
    val jerseyClient = "org.glassfish.jersey.core" % "jersey-client" % Versions.jersey
    val jerseyConnector = "org.glassfish.jersey.connectors" % "jersey-grizzly-connector" % Versions.jersey
  }

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  }

  import Dependencies.Compile._

  val dryadCore: Seq[ModuleID] = Seq(cglib, logging, config, snakeYAML, guava, rxScala, Test.scalaTest)

  val dryadConsul: Seq[ModuleID] = Seq(jerseyClient, jerseyConnector, consul, Test.scalaTest)
}