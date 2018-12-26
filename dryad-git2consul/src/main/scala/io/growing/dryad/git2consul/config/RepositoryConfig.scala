package io.growing.dryad.git2consul.config

/**
 * Component:
 * Description:
 * Date: 2018-12-25
 *
 * @author AI
 */
final case class RepositoryConfig(extensions: Seq[String])

object RepositoryConfig {

  def empty: RepositoryConfig = RepositoryConfig(Seq.empty)

}
