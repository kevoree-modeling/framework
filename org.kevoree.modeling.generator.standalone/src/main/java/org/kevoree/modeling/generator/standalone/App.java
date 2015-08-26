package org.kevoree.modeling.generator.standalone;

import org.kevoree.modeling.action.VersionAnalyzer;
import org.kevoree.modeling.generator.GenerationContext;
import org.kevoree.modeling.generator.Generator;
import org.kevoree.modeling.generator.JSOptimizer;
import org.kevoree.modeling.generator.mavenplugin.GenModelPlugin;
import org.kevoree.modeling.generator.mavenplugin.TscRunner;
import org.kevoree.modeling.java2typescript.SourceTranslator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class App {

    public static void main(final String[] args) throws IOException, InterruptedException {
        ThreadGroup tg = new ThreadGroup("KMFCompiler");
        Thread t = new Thread(tg, new Runnable() {
            @Override
            public void run() {
                try {
                    if (args.length != 1 && args.length != 2) {
                        System.out.println("Bad arguments : <metaModelFile> [<js/jar/umd>]");
                        return;
                    }
                    String ecore = args[0];
                    File metaModelFile = new File(ecore);
                    if (!metaModelFile.exists()) {
                        System.out.println("Bad arguments : <metaModelFile> [<js/jar>] : metaModelFile not exists");
                    }
                    if (!metaModelFile.getName().contains(".")) {
                        System.out.println("Bad input file " + metaModelFile.getName() + " , must be .mm");
                    }
                    Boolean js = false;
                    Boolean umd = false;
                    if (args.length > 1 && args[1].toLowerCase().equals("js")) {
                        js = true;
                    }
                    if (args.length > 1 && args[1].toLowerCase().equals("umd")) {
                        js = true;
                        umd = true;
                    }
                    GenerationContext ctx = new GenerationContext();
                    File masterOut = new File("gen");
                    if (System.getProperty("output") != null) {
                        masterOut = new File(System.getProperty("output"));
                    }
                    masterOut.mkdirs();
                    File srcOut = new File(masterOut, "java");
                    srcOut.mkdirs();
                    File jsDir = new File(masterOut, "js");
                    jsDir.mkdirs();
                    ctx.setMetaModel(metaModelFile);
                    String mmName = metaModelFile.getName().substring(0, metaModelFile.getName().lastIndexOf("."));
                    ctx.setMetaModelName(mmName.substring(0, 1).toUpperCase() + mmName.substring(1));
                    ctx.setTargetSrcDir(srcOut);
                    ctx.setVersion(VersionAnalyzer.getVersion(metaModelFile));
                    Generator generator = new Generator();
                    generator.execute(ctx);
                    if (js) {
                        Files.createDirectories(jsDir.toPath());
                        Path libDts = Paths.get(jsDir.toPath().toString(), GenModelPlugin.LIB_D_TS);
                        Files.copy(this.getClass().getClassLoader().getResourceAsStream("tsc/" + GenModelPlugin.LIB_D_TS), libDts, StandardCopyOption.REPLACE_EXISTING);
                        Files.copy(getClass().getClassLoader().getResourceAsStream(GenModelPlugin.TSC_JS), Paths.get(jsDir.toPath().toString(), GenModelPlugin.TSC_JS), StandardCopyOption.REPLACE_EXISTING);
                        SourceTranslator sourceTranslator = new SourceTranslator();
                        sourceTranslator.additionalAppend = "org.kevoree.modeling.microframework.browser.ts";
                        sourceTranslator.exportPackage = new String[]{"org"};
                        String[] javaClassPath = System.getProperty("java.class.path").split(File.pathSeparator);
                        for (String dep : javaClassPath) {
                            if (dep.endsWith(".jar")) {
                                sourceTranslator.getAnalyzer().addClasspath(dep);
                            }
                        }
                        sourceTranslator.translateSources(srcOut.getAbsolutePath(), jsDir.getAbsolutePath(), ctx.getMetaModelName(), false, false, umd);
                        TscRunner runner = new TscRunner();
                        Path tscPath = Paths.get(jsDir.toPath().toString(), GenModelPlugin.TSC_JS);
                        runner.runTsc(jsDir, jsDir, null, true, umd);

                        File input = new File(jsDir.getAbsolutePath(), ctx.getMetaModelName() + ".js");
                        File outputMin = new File(jsDir.getAbsolutePath(), ctx.getMetaModelName() + ".min.js");
                        JSOptimizer.optimize(input, outputMin);


                        tscPath.toFile().delete();
                        libDts.toFile().delete();
                    }
                    System.out.println("Output : " + masterOut.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        t.join();


        Thread[] subT = new Thread[1000];
        tg.enumerate(subT, true);
        for (Thread sub : subT) {
            try {
                if (sub != null) {
                    sub.interrupt();
                    sub.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        tg.interrupt();
        tg.stop();
    }

}
