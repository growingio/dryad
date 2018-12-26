package io.growing.dryad.git2consul.writer

import java.nio.file.Path

import scala.concurrent.Future

/**
 * Component:
 * Description:
 * Date: 2018-12-24
 *
 * @author AI
 */
trait ConfigurationWriter {

  def write(file: Path, name: String): Future[Boolean]

}
