package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.SassCompile;
import lombok.Getter;
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
@Getter
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
                SassCompile sassCompile = project.getTasks().create(i == 1 ? taskName : taskName + i, SassCompile.class);
                i++;

                sassCompile.setGroup(BasePlugin.BUILD_GROUP);
                sassCompile.setDescription("Compile sass and scss files for the " + sourceSet.getName() + " source set");

                sassCompile.setSourceDir(srcDir);

                Copy processResources = (Copy) project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());

                sassCompile.getConventionMapping().map("destinationDir", () -> {
                            if (jSassBasePlugin.getExtension().isInplace()) {
                                return srcDir;
                            } else {
                                return processResources.getDestinationDir();
                            }
                        });

                processResources.dependsOn(sassCompile);
            }
        });
    }
}
