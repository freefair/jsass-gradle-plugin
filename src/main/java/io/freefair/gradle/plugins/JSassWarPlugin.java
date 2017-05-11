package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.SassCompile;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.bundling.War;

import java.io.File;

public class JSassWarPlugin implements Plugin<Project> {

    private JSassBasePlugin jSassBasePlugin;

    @Override
    public void apply(Project project) {

        jSassBasePlugin = project.getPlugins().apply(JSassBasePlugin.class);
        project.getPlugins().apply(WarPlugin.class);

        SassCompile compileWebappSass = project.getTasks().create("compileWebappSass", SassCompile.class);
        compileWebappSass.setGroup(BasePlugin.BUILD_GROUP);
        compileWebappSass.setDescription("Compile sass and scss files for the webapp");

        WarPluginConvention warPluginConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        compileWebappSass.setSourceDir(warPluginConvention.getWebAppDir());

        project.afterEvaluate(p -> {
            War war = (War) p.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
            if (jSassBasePlugin.getExtension().isInplace()) {
                compileWebappSass.setDestinationDir(warPluginConvention.getWebAppDir());
            } else {
                compileWebappSass.setDestinationDir(new File(p.getBuildDir(), "generated/webappCss"));
                war.from(compileWebappSass.getDestinationDir());
            }
            war.dependsOn(compileWebappSass);
        });
    }
}
