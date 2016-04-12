package io.growing.dryad.exception

/**
 * Component:
 * Description:
 * Date: 16/3/28
 *
 * @author Andy Ai
 */
class ConfigurationNotFoundException(name: String) extends RuntimeException(s"Configuration cannot found, name: $name")