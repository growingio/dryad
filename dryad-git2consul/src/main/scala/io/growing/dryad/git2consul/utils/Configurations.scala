package io.growing.dryad.git2consul.utils

import java.nio.file.{ FileVisitOption, Files, Path }

import com.typesafe.config.ConfigFactory
import configs.syntax._
import io.growing.dryad.git2consul.config.RepositoryConfig

import scala.collection.mutable.ListBuffer
import scala.util.Try
import com.google.common.io.{ Files ⇒ FilesUtils }

/**
 * Component:
 * Description:
 * Date: 2018-12-26
 *
 * @author AI
 */
object Configurations {

  @volatile lazy val repositoryConfigName = ".git2consul.conf"

  def getConfigurations(root: Path): Seq[Configuration] = {
    val stream = Files.walk(root)
    val config = parseRepositoryConfig(root)
    val hiddens = new ListBuffer[Path]
    val results = new ListBuffer[Path]
    try {
      stream.forEach((path: Path) ⇒ {
        if (Files.isDirectory(path)) {
          if (isHidden(path)) {
            hiddens += path
          }
        } else {
          if (config.extensions.isEmpty || config.extensions.contains(FilesUtils.getFileExtension(path.getFileName.toString))) {
            results += path
          }
        }
      })
    } finally {
      Try(stream.close())
    }
    results.filterNot { path ⇒
      hiddens.exists(hidden ⇒ path.toString.contains(hidden.toString))
    }
    val rootName = root.toString
    results.collect {
      case path: Path if !isHidden(path) && !hiddens.exists(hidden ⇒ path.toString.contains(hidden.toString)) ⇒
        Configuration(path.toString.replace(rootName, ""), path)
    }
  }

  private[utils] def parseRepositoryConfig(path: Path): RepositoryConfig = {
    val configFile = path.resolve(repositoryConfigName)
    if (Files.notExists(configFile)) {
      RepositoryConfig.empty
    } else {
      ConfigFactory.parseFile(configFile.toFile).extract[RepositoryConfig].value
    }
  }

  private[utils] def isHidden(path: Path): Boolean = {
    Files.isHidden(path) || path.getFileName.toString.startsWith(".")
  }

}
