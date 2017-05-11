package io.freefair.gradle.plugins.jsass;

import com.google.gson.Gson;
import io.bit3.jsass.*;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.annotation.DebugFunction;
import io.bit3.jsass.annotation.ErrorFunction;
import io.bit3.jsass.annotation.WarnFunction;
import io.bit3.jsass.importer.Importer;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.file.*;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.*;

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
public class CompileSass extends ConventionTask {

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

        options.setFunctionProviders(new ArrayList<>(getFunctionProviders()));
        options.getFunctionProviders().add(new LoggingFunctionProvider());
        options.setHeaderImporters(getHeaderImporters());
        options.setImporters(getImporters());
        if (getIncludePaths() != null) {
            options.setIncludePaths(new ArrayList<>(getIncludePaths().getFiles()));
        }
        options.setIndent(getIndent());
        options.setLinefeed(getLinefeed());
        options.setOmitSourceMapUrl(isOmitSourceMapUrl());
        options.setOutputStyle(getOutputStyle());
        options.setPluginPath(getPluginPath());
        options.setPrecision(getPrecision());
        options.setSourceComments(isSourceComments());
        options.setSourceMapContents(isSourceMapContents());
        options.setSourceMapEmbed(isSourceMapEmbed());
        options.setSourceMapRoot(getSourceMapRoot());

        File realSourceDir = new File(sourceDir, sassPath);

        File fakeDestinationDir = new File(sourceDir, cssPath);
        File realDestinationDir = new File(getDestinationDir(), cssPath);

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

                    if(isSourceMapEnabled()) {
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
                        if (isSourceMapEnabled()) {
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
    private String indent;

    @Input
    private String linefeed;

    /**
     * Disable sourceMappingUrl in css output.
     */
    @Input
    private boolean omitSourceMapUrl;

    /**
     * Output style for the generated css code.
     */
    @Input
    private OutputStyle outputStyle;

    @Input
    @Optional
    private String pluginPath;

    /**
     * Precision for outputting fractional numbers.
     */
    @Input
    private int precision;

    /**
     * If you want inline source comments.
     */
    @Input
    private boolean sourceComments;

    /**
     * Embed include contents in maps.
     */
    @Input
    private boolean sourceMapContents;

    /**
     * Embed sourceMappingUrl as data uri.
     */
    @Input
    private boolean sourceMapEmbed;

    @Input
    private boolean sourceMapEnabled;

    @Input
    @Optional
    private URI sourceMapRoot;

    public class LoggingFunctionProvider {

        @WarnFunction
        public void warn(String message) {
            getLogger().warn(message);
        }

        @ErrorFunction
        public void error(String message) {
            getLogger().error(message);
        }

        @DebugFunction
        public void debug(String message) {
            getLogger().info(message);
        }
    }
}
