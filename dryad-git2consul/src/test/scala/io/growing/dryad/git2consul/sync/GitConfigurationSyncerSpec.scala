package io.growing.dryad.git2consul.sync

import com.typesafe.config.ConfigFactory
import io.growing.dryad.git2consul.config.Git2ConsulConfig
import io.growing.dryad.git2consul.sync.impl.GitConfigurationSyncer
import io.growing.dryad.git2consul.writer.impl.ConsulConfigurationWriter
import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018-12-25
 *
 * @author AI
 */
class GitConfigurationSyncerSpec extends FunSuite {

  test("sync") {
    val config = Git2ConsulConfig.parse(ConfigFactory.load())
    val writer = new ConsulConfigurationWriter(config)
    val syncer = new GitConfigurationSyncer(config, writer)
    syncer.sync()
  }

}
