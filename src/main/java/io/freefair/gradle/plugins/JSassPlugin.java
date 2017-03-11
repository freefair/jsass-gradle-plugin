package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
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
    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java", appliedPlugin -> applyForJava(project));

        project.getPluginManager().withPlugin("war", appliedPlugin -> applyForWar(project));
    }

    private void applyForWar(Project project) {
        WarPluginConvention warPluginConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        CompileSass compileWebappSass = project.getTasks().create("compileWebappSass", CompileSass.class);

        compileWebappSass.setSourceDir(warPluginConvention.getWebAppDir());
        compileWebappSass.setDestinationDir(warPluginConvention.getWebAppDir());

        War war = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        war.dependsOn(compileWebappSass);
    }

    private void applyForJava(Project project) {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            String taskName = sourceSet.getTaskName("compile", "Sass");

            Set<File> srcDirs = sourceSet.getResources().getSrcDirs();

            int i = 1;
            for (File srcDir : srcDirs) {
                CompileSass compileSass = project.getTasks().create(i == 1 ? taskName : taskName + i, CompileSass.class);
                i++;

                compileSass.setSourceDir(srcDir);

                Copy processResources = (Copy) project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());
                compileSass.setDestinationDir(processResources.getDestinationDir());

                compileSass.setGroup("build");
                compileSass.setDescription("Compile sass and scss files for the " + sourceSet.getName() + " source set");

                processResources.dependsOn(compileSass);
            }

        });
    }
}
