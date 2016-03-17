import sbt._

object Dependencies {

  object Versions {
    val scala = "2.11.7"
    val akkaHttp = "2.0.2"
    val scalaTest = "2.2.6"
  }

  object Compile {
    val akkaStream = "com.typesafe.akka" %% "akka-stream-experimental" % Versions.akkaHttp
    val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core-experimental" % Versions.akkaHttp
    val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % Versions.akkaHttp
  }

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  }

  import Compile._

  val dependencies: Seq[ModuleID] = Seq(akkaStream, akkaHttpCore, akkaHttp, Test.scalaTest)
}