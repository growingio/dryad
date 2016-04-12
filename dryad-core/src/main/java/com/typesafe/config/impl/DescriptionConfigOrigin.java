package com.typesafe.config.impl;

import com.typesafe.config.ConfigOrigin;

import java.net.URL;
import java.util.List;

/**
 * Component:
 * Description:
 * Date: 16/4/12
 *
 * @author Andy Ai
 */
public class DescriptionConfigOrigin implements ConfigOrigin {
    private SimpleConfigOrigin simpleConfigOrigin;

    public DescriptionConfigOrigin(String description) {
        simpleConfigOrigin = SimpleConfigOrigin.newSimple(description);
    }

    @Override
    public String description() {
        return simpleConfigOrigin.description();
    }

    @Override
    public String filename() {
        return simpleConfigOrigin.filename();
    }

    @Override
    public URL url() {
        return simpleConfigOrigin.url();
    }

    @Override
    public String resource() {
        return simpleConfigOrigin.resource();
    }

    @Override
    public int lineNumber() {
        return simpleConfigOrigin.lineNumber();
    }

    @Override
    public List<String> comments() {
        return simpleConfigOrigin.comments();
    }

    @Override
    public ConfigOrigin withComments(List<String> comments) {
        return simpleConfigOrigin.withComments(comments);
    }

    @Override
    public ConfigOrigin withLineNumber(int lineNumber) {
        return simpleConfigOrigin.withLineNumber(lineNumber);
    }
}
