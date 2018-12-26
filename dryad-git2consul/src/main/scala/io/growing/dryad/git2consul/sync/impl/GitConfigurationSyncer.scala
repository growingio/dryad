package io.growing.dryad.git2consul.sync.impl

import java.nio.file.{ Files, Path }

import com.google.common.hash.Hashing
import com.google.common.io.{ Files ⇒ FilesUtils }
import io.growing.dryad.git2consul.cache.Caches
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.sync.ConfigurationSyncer
import io.growing.dryad.git2consul.utils.Configurations
import io.growing.dryad.git2consul.writer.ConfigurationWriter
import org.apache.logging.log4j.scala.Logging
import org.eclipse.jgit.api.Git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * Component:
 * Description:
 * Date: 2018-12-24
 *
 * @author AI
 */
class GitConfigurationSyncer(config: Git2ConsulConfig, configurationWriter: ConfigurationWriter) extends ConfigurationSyncer with Logging {
  private[this] val writeTimeout = 1.minutes

  override def sync(): Unit = {
    val root = Files.createTempDirectory("git2consul")
    Git.cloneRepository().setDirectory(root.toFile).setURI(config.git.uri).call()
    doSync(root)
  }

  private[impl] def doSync(root: Path): Unit = {
    val configurations = Configurations.getConfigurations(root)
    val futures = configurations.map { configuration ⇒
      val filename = configuration.filename
      val hash = Hashing.murmur3_128().hashBytes(FilesUtils.toByteArray(configuration.path.toFile)).toString
      val version: String = Option(Caches.versions.get(filename)).getOrElse("")
      if (hash == version) {
        logger.info(s"file [$filename] not changed")
        Future.successful(Unit)
      } else {
        configurationWriter.write(configuration.path, filename).map {
          case true ⇒
            logger.info(s"write [$filename] success")
            Caches.versions.put(filename, hash)
          case false ⇒ logger.error(s"write [$filename] failure")
        }
      }
    }
    Await.result(Future.sequence(futures), writeTimeout)
  }

}
