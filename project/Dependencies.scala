import sbt.Keys._
import sbt._

object Dependencies {

  object Versions {
    val scala = "2.13.3"
    val config = "1.4.0"
    val log4j2 = "2.13.3"
    val jersey = "2.22.2"
    val configs = "0.4.4"
    val ehcache = "3.8.1"
    val caffeine = "2.8.5"
    val guava = "29.0-jre"
    val scala213 = "2.13.1"
    val scalatest = "3.2.0"
    val scala212 = "2.12.12"
    val scala211 = "2.11.12"
    val cglibNodep = "3.3.0"
    val scalaUtils = "1.0.20"
    val scalaLogging = "3.9.2"
    val consulClient = "1.4.2"
    val scalaLibrary = "2.13.3"
    val undertowCore = "2.1.3.Final"
    val jacksonModuleScala = "2.11.1"
    val jgit = "5.4.0.201906121030-r"
    val lombok = "1.18.12"
  }

  object Compiles {
    val lombok = "org.projectlombok" % "lombok" % Versions.lombok
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
    val caffeine: ModuleID = "com.github.ben-manes.caffeine" % "caffeine" % Versions.caffeine
    val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
    val jackson: ModuleID = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScala
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Dependencies.Compiles._

  val l = libraryDependencies

  val dryadCore = l ++= Seq(cglib, logging, config, guava, lombok, caffeine, Tests.scalaTest)

  val dryadConsul = l ++= Seq(consul, jackson, Tests.scalaTest)

  val dryadCluster = l ++= Seq(Tests.scalaTest, Compiles.undertow % Test)

  val git2Consul = l ++= log4j2 ++ Seq(logging, jgit, ehcache, config, configs, guava,
    jackson, undertow, consul, scalaUtils, Tests.scalaTest)

  val crossScalaVersions: Seq[String] = Seq(Versions.scala211, Versions.scala212)

}
