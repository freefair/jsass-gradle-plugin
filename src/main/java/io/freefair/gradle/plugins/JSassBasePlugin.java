package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.SassCompile;
import io.freefair.gradle.plugins.jsass.JSassExtension;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.ConventionMapping;

/**
 * @author Lars Grefer
 */
public class JSassBasePlugin implements Plugin<Project> {

    @Getter
    private JSassExtension extension;

    @Override
    public void apply(Project project) {
        this.extension = project.getExtensions().create("jSass", JSassExtension.class);

        project.getTasks().withType(SassCompile.class, compileSass -> {
            ConventionMapping conventionMapping = compileSass.getConventionMapping();

            conventionMapping.map("indent", () -> extension.getIndent());
            conventionMapping.map("linefeed", () -> extension.getLinefeed());
            conventionMapping.map("omitSourceMapUrl", () -> extension.isOmitSourceMapUrl());
            conventionMapping.map("outputStyle", () -> extension.getOutputStyle());
            conventionMapping.map("precision", () -> extension.getPrecision());
            conventionMapping.map("sourceComments", () -> extension.isSourceComments());
            conventionMapping.map("sourceMapContents", () -> extension.isSourceMapContents());
            conventionMapping.map("sourceMapEmbed", () -> extension.isSourceMapEmbed());
            conventionMapping.map("sourceMapEnabled", () -> extension.isSourceMapEnabled());
        });
    }
}
