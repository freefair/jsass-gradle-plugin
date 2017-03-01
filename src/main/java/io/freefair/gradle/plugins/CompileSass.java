package io.freefair.gradle.plugins;

import io.bit3.jsass.*;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.importer.Importer;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
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

    @InputDirectory
    private File sourceDir;

    @OutputDirectory
    private File destinationDir;

    @TaskAction
    public void compileSass() {
        Compiler compiler = new Compiler();
        Options options = new Options();

        options.setFunctionProviders(functionProviders);
        options.setHeaderImporters(headerImporters);
        options.setImporters(importers);
        if(includePaths != null) {
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
        options.setSourceMapFile(sourceMapFile);
        options.setSourceMapRoot(sourceMapRoot);

        getProject().fileTree(sourceDir).visit(new FileVisitor() {
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

                    File out = new File(destinationDir, pathString);

                    options.setIsIndentedSyntaxSrc(name.endsWith(".sass"));
                    compile(in, out, compiler, options);
                }
            }
        });


    }

    private void compile(File in, File out, Compiler compiler, Options options) {
        try {
            Output output = compiler.compileFile(in.toURI(), out.toURI(), options);
            ResourceGroovyMethods.write(out, output.getCss());
        } catch (CompilationException e) {
            SassError sassError = new Gson().fromJson(e.getErrorJson(), SassError.class);

            getLogger().error("{}:{}:{}", sassError.getFile(), sassError.getLine(), sassError.getColumn());
            getLogger().error(e.getErrorMessage());

            throw new TaskExecutionException(this, e);
        } catch (IOException e) {
            getLogger().error(e.getLocalizedMessage());
            throw new TaskExecutionException(this, e);
        }
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

    /**
     * Path to source map file. Enables the source map generating. Used to create sourceMappingUrl.
     */
    @Input
    @Optional
    private URI sourceMapFile;

    @Input
    @Optional
    private URI sourceMapRoot;
}
