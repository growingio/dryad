import sbt.Keys._
import sbt._

object Publish extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
      } else {
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
      }
    },
    pomExtra :=
      <url>https://github.com/growingio/dryad</url>
      <licenses>
        <license>
          <name>Apache License</name>
          <url>http://www.apache.org/licenses/</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:growingio/dryad.git</url>
        <connection>scm:git:git@github.com:growingio/dryad.git</connection>
      </scm>
      <developers>
        <developer>
          <id>yanbo.ai</id>
          <name>Andy Ai</name>
          <url>http://aiyanbo.github.io/</url>
        </developer>
      </developers>)

}

object DontPublish extends AutoPlugin {

  override def requires: Plugins = plugins.IvyPlugin

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
    publishArtifact := false,
    publish := Unit,
    publishLocal := Unit)

}
