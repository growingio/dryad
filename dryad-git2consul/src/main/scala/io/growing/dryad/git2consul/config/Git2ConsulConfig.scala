package io.growing.dryad.git2consul.config

import com.typesafe.config.Config
import configs.syntax._

/**
 * Component:
 * Description:
 * Date: 2018-12-25
 *
 * @author AI
 */
final case class Git2ConsulConfig(git: GitConfig, consul: ConsulConfig, server: ServerConfig, underlying: Config)

object Git2ConsulConfig {

  def parse(config: Config): Git2ConsulConfig = {
    val git = config.get[GitConfig]("git2consul.git")
    val consul = config.get[ConsulConfig]("git2consul.consul")
    val server = config.get[ServerConfig]("git2consul.server")
    Git2ConsulConfig(git.value, consul.value, server.value, config)
  }

}
