package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.JSassExtension;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class JSassBasePlugin implements Plugin<Project> {

    @Getter
    private JSassExtension extension;

    @Override
    public void apply(Project project) {
        this.extension = project.getExtensions().create("jSass", JSassExtension.class);
    }
}
