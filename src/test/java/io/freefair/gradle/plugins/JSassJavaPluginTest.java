package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.SassCompile;
import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by larsgrefer on 03.05.17.
 */
public class JSassJavaPluginTest {

    private Project project;

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Before
    public void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(testProjectDir.getRoot())
                .build();
    }

    @Test
    public void testSources() throws IOException {
        File cssFolder = testProjectDir.newFolder("src", "main", "resources", "sass");

        File mainCss = new File(cssFolder, "main.scss");

        mainCss.createNewFile();

        Files.write("body { color: red; }", mainCss, Charset.defaultCharset());

        JSassJavaPlugin jSassJavaPlugin = project.getPlugins().apply(JSassJavaPlugin.class);

        SassCompile compileSass = (SassCompile) project.getTasks().getByName("compileSass");

        compileSass.compileSass();

        project.getBuildDir();

    }


}