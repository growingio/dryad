import sbt._
import Keys._

object Dependencies {

  object Versions {
    val config = "1.3.3"
    val log4j2 = "2.11.1"
    val ehcache = "3.6.3"
    val jersey = "2.22.2"
    val configs = "0.4.4"
    val logging = "3.9.0"
    val scalatest = "3.0.5"
    val scala211 = "2.11.12"
    val guava = "27.0.1-jre"
    val scalaUtils = "1.0.4"
    val cglibNodep = "3.2.10"
    val consulClient = "1.3.0"
    val scalaLibrary = "2.12.8"
    val jacksonModuleScala = "2.9.8"
    val undertowCore = "2.0.17.Final"
    val jgit = "5.2.0.201812061821-r"
  }

  object Compiles {
    val config: ModuleID = "com.typesafe" % "config" % Versions.config
    val cglib: ModuleID = "cglib" % "cglib-nodep" % Versions.cglibNodep
    val guava: ModuleID = "com.google.guava" % "guava" % Versions.guava
    val ehcache: ModuleID = "org.ehcache" % "ehcache" % Versions.ehcache
    val log4j2: Seq[ModuleID] = Seq(
      "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
      "org.apache.logging.log4j" % "log4j-api" % Versions.log4j2,
      "org.apache.logging.log4j" % "log4j-core" % Versions.log4j2,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.log4j2)
    val configs: ModuleID = "com.github.kxbmap" %% "configs" % Versions.configs
    val jgit: ModuleID = "org.eclipse.jgit" % "org.eclipse.jgit" % Versions.jgit
    val scalaUtils: ModuleID = "org.jmotor" %% "scala-utils" % Versions.scalaUtils
    val undertow: ModuleID = "io.undertow" % "undertow-core" % Versions.undertowCore
    val consul: ModuleID = "com.orbitz.consul" % "consul-client" % Versions.consulClient
    val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Versions.logging
    val jackson: ModuleID = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScala
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Dependencies.Compiles._

  val l = libraryDependencies

  val dryadCore = l ++= Seq(configs, cglib, logging, config, guava, Tests.scalaTest)

  val dryadConsul = l ++= Seq(consul, Tests.scalaTest)

  val dryadCluster = l ++= Seq(Tests.scalaTest)

  val git2Consul = l ++= log4j2 ++ Seq(jgit, ehcache, config, configs, guava,
    jackson, undertow, consul, scalaUtils, Tests.scalaTest)

  val crossScalaVersions: Seq[String] = Seq(Versions.scalaLibrary, "2.11.11")

}
