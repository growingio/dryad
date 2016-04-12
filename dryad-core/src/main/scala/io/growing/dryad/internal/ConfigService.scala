package io.growing.dryad.internal

import rx.subjects.PublishSubject

/**
 * Component:
 * Description:
 * Date: 16/3/25
 *
 * @author Andy Ai
 */
trait ConfigService {

  def get(name: String): Configuration

  def subject(): PublishSubject[Configuration]

}
