import sbt.Keys._
import sbt._

object Dependencies {

  object Versions {
    val config = "1.3.4"
    val scala = "2.13.0"
    val log4j2 = "2.12.0"
    val ehcache = "3.7.1"
    val jersey = "2.22.2"
    val configs = "0.4.4"
    val guava = "28.0-jre"
    val scalatest = "3.0.8"
    val scala211 = "2.11.12"
    val scala212 = "2.12.8"
    val scala213 = "2.13.0"
    val scalaUtils = "1.0.8"
    val cglibNodep = "3.2.12"
    val scalaLogging = "3.9.2"
    val consulClient = "1.3.6"
    val jacksonModuleScala = "2.9.9"
    val undertowCore = "2.0.22.Final"
    val jgit = "5.4.0.201906121030-r"
  }

  object Compiles {
    val config: ModuleID = "com.typesafe" % "config" % Versions.config
    val cglib: ModuleID = "cglib" % "cglib-nodep" % Versions.cglibNodep
    val guava: ModuleID = "com.google.guava" % "guava" % Versions.guava
    val ehcache: ModuleID = "org.ehcache" % "ehcache" % Versions.ehcache
    val log4j2: Seq[ModuleID] = Seq(
      "org.apache.logging.log4j" % "log4j-api" % Versions.log4j2,
      "org.apache.logging.log4j" % "log4j-core" % Versions.log4j2,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.log4j2)
    val configs: ModuleID = "com.github.kxbmap" %% "configs" % Versions.configs
    val jgit: ModuleID = "org.eclipse.jgit" % "org.eclipse.jgit" % Versions.jgit
    val scalaUtils: ModuleID = "org.jmotor" %% "scala-utils" % Versions.scalaUtils
    val undertow: ModuleID = "io.undertow" % "undertow-core" % Versions.undertowCore
    val consul: ModuleID = "com.orbitz.consul" % "consul-client" % Versions.consulClient
    val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
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

  val git2Consul = l ++= log4j2 ++ Seq(logging, jgit, ehcache, config, configs, guava,
    jackson, undertow, consul, scalaUtils, Tests.scalaTest)

  val crossScalaVersions: Seq[String] = Seq(Versions.scala211, Versions.scala212)

}
