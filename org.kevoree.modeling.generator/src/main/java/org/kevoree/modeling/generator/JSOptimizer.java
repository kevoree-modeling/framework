package org.kevoree.modeling.generator;

import com.google.javascript.jscomp.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class JSOptimizer {

    public static void optimize(File in, File out) throws IOException {
        com.google.javascript.jscomp.Compiler.setLoggingLevel(Level.INFO);
        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        WarningLevel.DEFAULT.setOptionsForWarningLevel(options);
        compiler.compile(SourceFile.fromCode("nop.js", ""), SourceFile.fromFile(in), options);
        for (JSError message : compiler.getWarnings()) {
            System.err.println("Warning message: " + message.toString());
        }
        for (JSError message : compiler.getErrors()) {
            System.err.println("Error message: " + message.toString());
        }
        FileWriter outputFile = new FileWriter(out);
        outputFile.write(compiler.toSource());
        outputFile.close();
    }

}
