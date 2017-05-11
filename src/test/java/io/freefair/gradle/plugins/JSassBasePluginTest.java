package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.SassCompile;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Lars Grefer
 */
public class JSassBasePluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void testPrecisionConvention() throws Exception {
        JSassBasePlugin basePlugin = project.getPlugins().apply(JSassBasePlugin.class);
        SassCompile testSass = project.getTasks().create("testSass", SassCompile.class);

        assertThat(testSass.getPrecision()).isEqualTo(basePlugin.getExtension().getPrecision());
    }

    @Test
    public void testPrecisionConvention_custom() throws Exception {
        JSassBasePlugin basePlugin = project.getPlugins().apply(JSassBasePlugin.class);
        SassCompile testSass = project.getTasks().create("testSass", SassCompile.class);

        basePlugin.getExtension().setPrecision(42);
        assertThat(testSass.getPrecision()).isEqualTo(42);

        basePlugin.getExtension().setPrecision(55);
        assertThat(testSass.getPrecision()).isEqualTo(55);
    }

}