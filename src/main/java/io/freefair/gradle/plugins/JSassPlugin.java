package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.Set;

/**
 * @author Lars Grefer
 */
public class JSassPlugin implements Plugin<Project> {

    private Project project;
    private JSassExtension extension;

    @Override
    public void apply(Project project) {
        this.project = project;
        this.extension = project.getExtensions().create("jSass", JSassExtension.class);

        project.getPluginManager().withPlugin("java", appliedPlugin -> applyForJava());

        project.getPluginManager().withPlugin("war", appliedPlugin -> applyForWar());
    }

    private void applyForWar() {
        CompileSass compileWebappSass = project.getTasks().create("compileWebappSass", CompileSass.class);
        compileWebappSass.setGroup(BasePlugin.BUILD_GROUP);
        compileWebappSass.setDescription("Compile sass and scss files for the webapp");

        WarPluginConvention warPluginConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        compileWebappSass.setSourceDir(warPluginConvention.getWebAppDir());

        project.afterEvaluate(project -> {
            War war = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
            if (extension.isInplace()) {
                compileWebappSass.setDestinationDir(warPluginConvention.getWebAppDir());
            } else {
                compileWebappSass.setDestinationDir(new File(project.getBuildDir(), "generated/webappCss"));
                war.from(compileWebappSass.getDestinationDir());
            }
            war.dependsOn(compileWebappSass);
        });

    }

    private void applyForJava() {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            String taskName = sourceSet.getTaskName("compile", "Sass");

            Set<File> srcDirs = sourceSet.getResources().getSrcDirs();

            int i = 1;
            for (File srcDir : srcDirs) {
                CompileSass compileSass = project.getTasks().create(i == 1 ? taskName : taskName + i, CompileSass.class);
                i++;

                compileSass.setGroup(BasePlugin.BUILD_GROUP);
                compileSass.setDescription("Compile sass and scss files for the " + sourceSet.getName() + " source set");

                compileSass.setSourceDir(srcDir);

                Copy processResources = (Copy) project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());

                project.afterEvaluate(project -> {
                    if (extension.isInplace()) {
                        compileSass.setDestinationDir(srcDir);
                    } else {
                        compileSass.setDestinationDir(processResources.getDestinationDir());
                    }
                    processResources.dependsOn(compileSass);
                });
            }
        });
    }
}
