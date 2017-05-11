package io.freefair.gradle.plugins.jsass;

import io.bit3.jsass.OutputStyle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JSassExtension {
    private boolean inplace = false;

    private String indent = "  ";

    private String linefeed = System.lineSeparator();

    private boolean omitSourceMapUrl = false;

    /**
     * Output style for the generated css code.
     */
    private OutputStyle outputStyle = OutputStyle.NESTED;

    /**
     * Precision for outputting fractional numbers.
     */
    private int precision = 8;

    /**
     * If you want inline source comments.
     */
    private boolean sourceComments = false;

    /**
     * Embed include contents in maps.
     */
    private boolean sourceMapContents = false;

    /**
     * Embed sourceMappingUrl as data uri.
     */
    private boolean sourceMapEmbed = false;

    private boolean sourceMapEnabled = true;
}
