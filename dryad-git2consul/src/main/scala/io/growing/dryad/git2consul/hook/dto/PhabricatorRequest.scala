package io.growing.dryad.git2consul.hook.dto

/**
 * Component:
 * Description:
 * Date: 2018-12-25
 *
 * @author AI
 */
final case class PhabricatorRequest(`object`: PhabricatorObject)

final case class PhabricatorObject(`type`: String, phid: String)
