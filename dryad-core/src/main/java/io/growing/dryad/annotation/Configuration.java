package io.growing.dryad.annotation;

import io.growing.dryad.parser.ConfigParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Component:
 * Description:
 * Date: 16/3/18
 *
 * @author Andy Ai
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
    String name();

    Class<? extends ConfigParser<?>> parser();
}
