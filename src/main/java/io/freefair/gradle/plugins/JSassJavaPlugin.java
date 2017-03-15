package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.CompileSass;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.util.Set;

/**
 * @author Lars Grefer
 */
public class JSassJavaPlugin implements Plugin<Project> {

    private JSassBasePlugin jSassBasePlugin;

    @Override
    public void apply(Project project) {
        jSassBasePlugin = project.getPlugins().apply(JSassBasePlugin.class);

        project.getPlugins().apply(JavaPlugin.class);

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

                project.afterEvaluate(p -> {
                    if (jSassBasePlugin.getExtension().isInplace()) {
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
