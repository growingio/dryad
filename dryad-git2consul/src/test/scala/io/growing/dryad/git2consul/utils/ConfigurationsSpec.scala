package io.growing.dryad.git2consul.utils

import java.nio.file.Files

import org.eclipse.jgit.api.Git
import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018-12-26
 *
 * @author AI
 */
class ConfigurationsSpec extends FunSuite {

  test("get configurations") {
    val root = Files.createTempDirectory("git2consul-test")
    Git.cloneRepository().setURI("https://github.com/aiyanbo/sbt-dependency-updates.git").setDirectory(root.toFile).call()
    val configurations = Configurations.getConfigurations(root)
    assert(configurations.exists(p ⇒ p.path.getFileName.toString == "LICENSE"))
    assert(configurations.exists(p ⇒ p.path.getFileName.toString == "version.sbt"))
    assert(configurations.exists(p ⇒ p.path.getFileName.toString == "README.md"))
    assert(!configurations.exists(p ⇒ p.path.getFileName.toString == ".gitignore"))
    assert(!configurations.exists(p ⇒ p.path.getFileName.toString == ".travis.yml"))
    assert(!configurations.exists(p ⇒ p.path.getFileName.toString == ".scalariform.conf"))
  }

}
