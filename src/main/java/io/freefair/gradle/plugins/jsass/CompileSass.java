package io.freefair.gradle.plugins.jsass;

import io.bit3.jsass.*;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.importer.Importer;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public class CompileSass extends DefaultTask {

    @InputFiles
    protected FileTree getSourceFiles() {
        ConfigurableFileTree files = getProject().fileTree(new File(sourceDir, sassPath));
        files.include(fileTreeElement -> fileTreeElement.getName().endsWith(".scss"));
        files.include(fileTreeElement -> fileTreeElement.getName().endsWith(".sass"));
        return files;
    }

    @OutputFiles
    protected FileTree getOutputFiles() {
        ConfigurableFileTree files = getProject().fileTree(new File(destinationDir, cssPath));
        files.include(fileTreeElement -> fileTreeElement.getName().endsWith(".css"));
        files.include(fileTreeElement -> fileTreeElement.getName().endsWith(".css.map"));
        return files;
    }

    @Internal
    private File sourceDir;

    @Internal
    private File destinationDir;

    @Input
    private String cssPath = "";

    @Input
    private String sassPath = "";

    @TaskAction
    public void compileSass() {
        Compiler compiler = new Compiler();
        Options options = new Options();

        options.setFunctionProviders(functionProviders);
        options.setHeaderImporters(headerImporters);
        options.setImporters(importers);
        if (includePaths != null) {
            options.setIncludePaths(new ArrayList<>(includePaths.getFiles()));
        }
        options.setIndent(indent);
        options.setLinefeed(linefeed);
        options.setOmitSourceMapUrl(omitSourceMapUrl);
        options.setOutputStyle(outputStyle);
        options.setPluginPath(pluginPath);
        options.setPrecision(precision);
        options.setSourceComments(sourceComments);
        options.setSourceMapContents(sourceMapContents);
        options.setSourceMapEmbed(sourceMapEmbed);
        options.setSourceMapRoot(sourceMapRoot);

        File realSourceDir = new File(sourceDir, sassPath);

        File fakeDestinationDir = new File(sourceDir, cssPath);
        File realDestinationDir = new File(destinationDir, cssPath);

        getProject().fileTree(realSourceDir).visit(new FileVisitor() {
            @Override
            public void visitDir(FileVisitDetails fileVisitDetails) {

            }

            @Override
            public void visitFile(FileVisitDetails fileVisitDetails) {
                String name = fileVisitDetails.getName();
                if (name.startsWith("_"))
                    return;

                if (name.endsWith(".scss") || name.endsWith(".sass")) {
                    File in = fileVisitDetails.getFile();

                    String pathString = fileVisitDetails.getRelativePath().getPathString();

                    pathString = pathString.substring(0, pathString.length() - 5) + ".css";

                    File realOut = new File(realDestinationDir, pathString);
                    File fakeOut = new File(fakeDestinationDir, pathString);
                    File realMap = new File(realDestinationDir, pathString + ".map");
                    File fakeMap = new File(fakeDestinationDir, pathString + ".map");

                    options.setIsIndentedSyntaxSrc(name.endsWith(".sass"));

                    if(sourceMapEnabled) {
                        options.setSourceMapFile(fakeMap.toURI());
                    } else {
                        options.setSourceMapFile(null);
                    }

                    try {
                        URI inputPath = in.getAbsoluteFile().toURI();

                        Output output = compiler.compileFile(inputPath, fakeOut.toURI(), options);

                        if (realOut.getParentFile().exists() || realOut.getParentFile().mkdirs()) {
                            ResourceGroovyMethods.write(realOut, output.getCss());
                        } else {
                            getLogger().error("Cannot write into {}", realOut.getParentFile());
                            throw new TaskExecutionException(CompileSass.this, null);
                        }
                        if (sourceMapEnabled) {
                            if (realMap.getParentFile().exists() || realMap.getParentFile().mkdirs()) {
                                ResourceGroovyMethods.write(realMap, output.getSourceMap());
                            } else {
                                getLogger().error("Cannot write into {}", realMap.getParentFile());
                                throw new TaskExecutionException(CompileSass.this, null);
                            }
                        }
                    } catch (CompilationException e) {
                        SassError sassError = new Gson().fromJson(e.getErrorJson(), SassError.class);

                        getLogger().error("{}:{}:{}", sassError.getFile(), sassError.getLine(), sassError.getColumn());
                        getLogger().error(e.getErrorMessage());

                        throw new TaskExecutionException(CompileSass.this, e);
                    } catch (IOException e) {
                        getLogger().error(e.getLocalizedMessage());
                        throw new TaskExecutionException(CompileSass.this, e);
                    }
                }
            }
        });
    }

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    private List<Object> functionProviders = new LinkedList<>();

    @Input
    @Optional
    private List<Importer> headerImporters = new LinkedList<>();

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    private Collection<Importer> importers = new LinkedList<>();

    /**
     * SassList of paths.
     */
    @InputFiles
    @Optional
    private FileCollection includePaths;

    @Input
    private String indent = "  ";

    @Input
    private String linefeed = System.lineSeparator();

    /**
     * Disable sourceMappingUrl in css output.
     */
    @Input
    private boolean omitSourceMapUrl = false;

    /**
     * Output style for the generated css code.
     */
    @Input
    private OutputStyle outputStyle = OutputStyle.NESTED;

    @Input
    @Optional
    private String pluginPath;

    /**
     * Precision for outputting fractional numbers.
     */
    @Input
    private int precision = 8;

    /**
     * If you want inline source comments.
     */
    @Input
    private boolean sourceComments = false;

    /**
     * Embed include contents in maps.
     */
    @Input
    private boolean sourceMapContents = false;

    /**
     * Embed sourceMappingUrl as data uri.
     */
    @Input
    private boolean sourceMapEmbed = false;

    @Input
    private boolean sourceMapEnabled = true;

    @Input
    @Optional
    private URI sourceMapRoot;
}
