package org.kevoree.modeling.generator.standalone;

import org.kevoree.modeling.action.VersionAnalyzer;
import org.kevoree.modeling.generator.GenerationContext;
import org.kevoree.modeling.generator.Generator;
import org.kevoree.modeling.generator.mavenplugin.GenModelPlugin;
import org.kevoree.modeling.generator.mavenplugin.HtmlTemplateGenerator;
import org.kevoree.modeling.generator.mavenplugin.TscRunner;
import org.kevoree.modeling.java2typescript.SourceTranslator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Created by duke on 7/16/14.
 */
public class App {

    public static void main(final String[] args) throws IOException, InterruptedException {
        ThreadGroup tg = new ThreadGroup("KMFCompiler");
        Thread t = new Thread(tg, new Runnable() {
            @Override
            public void run() {
                try {
                    if (args.length != 1 && args.length != 2) {
                        System.out.println("Bad arguments : <metaModelFile> [<js/jar>]");
                        return;
                    }
                    String ecore = args[0];
                    File metaModelFile = new File(ecore);
                    if (!metaModelFile.exists()) {
                        System.out.println("Bad arguments : <metaModelFile> [<js/jar>] : metaModelFile not exists");
                    }
                    if (!metaModelFile.getName().contains(".")) {
                        System.out.println("Bad input file " + metaModelFile.getName() + " , must be .mm, .ecore or .xsd");
                    }
                    Boolean js = false;
                    if (args.length > 1 && args[1].toLowerCase().equals("js")) {
                        js = true;
                    }
                    GenerationContext ctx = new GenerationContext();
                    File masterOut = new File("gen");
                    if (System.getProperty("output") != null) {
                        masterOut = new File(System.getProperty("output"));
                    }
                    File resourceOut = new File("resources");
                    if (System.getProperty("resources") != null) {
                        resourceOut = new File(System.getProperty("resources"));
                    }
                    masterOut.mkdirs();
                    File srcOut = new File(masterOut, "java");
                    srcOut.mkdirs();
                    File jsDir = new File(masterOut, "js");
                    jsDir.mkdirs();
                    ctx.setMetaModel(metaModelFile);
                    String mmName = metaModelFile.getName().substring(0, metaModelFile.getName().lastIndexOf("."));
                    ctx.setMetaModelName(mmName.substring(0,1).toUpperCase() + mmName.substring(1));
                    ctx.setTargetSrcDir(srcOut);
                    ctx.setVersion(VersionAnalyzer.getVersion(metaModelFile));
                    Generator generator = new Generator();
                    generator.execute(ctx);
                    if (js) {
                        if (!resourceOut.exists()) {
                            resourceOut.mkdirs();
                        }
                        Files.createDirectories(jsDir.toPath());
                        Path javaLibJs = Paths.get(jsDir.toPath().toString(), GenModelPlugin.JAVA_LIB_JS);
                        Path libDts = Paths.get(jsDir.toPath().toString(), GenModelPlugin.LIB_D_TS);
                        Files.copy(this.getClass().getClassLoader().getResourceAsStream("tsc/" + GenModelPlugin.LIB_D_TS), libDts, StandardCopyOption.REPLACE_EXISTING);
                        Files.copy(getClass().getClassLoader().getResourceAsStream(GenModelPlugin.KMF_LIB_D_TS), Paths.get(jsDir.toPath().toString(), GenModelPlugin.KMF_LIB_D_TS), StandardCopyOption.REPLACE_EXISTING);
                        Path kmfLibJs = Paths.get(jsDir.toPath().toString(), GenModelPlugin.KMF_LIB_JS);
                        Files.copy(this.getClass().getClassLoader().getResourceAsStream(GenModelPlugin.KMF_LIB_JS), kmfLibJs, StandardCopyOption.REPLACE_EXISTING);
                        Files.copy(getClass().getClassLoader().getResourceAsStream(GenModelPlugin.TSC_JS), Paths.get(jsDir.toPath().toString(), GenModelPlugin.TSC_JS), StandardCopyOption.REPLACE_EXISTING);
                        SourceTranslator sourceTranslator = new SourceTranslator();
                        String[] javaClassPath = System.getProperty("java.class.path").split(File.pathSeparator);
                        for (String dep : javaClassPath) {
                            if (dep.endsWith(".jar")) {
                                sourceTranslator.getAnalyzer().addClasspath(dep);
                            }
                        }
                        sourceTranslator.translateSources(srcOut.getAbsolutePath(), jsDir.getAbsolutePath(), ctx.getMetaModelName());
                        System.out.print("Transpile to JS using TSC...");
                        TscRunner runner = new TscRunner();
                        Path tscPath = Paths.get(jsDir.toPath().toString(), GenModelPlugin.TSC_JS);
                        runner.runTsc(tscPath.toFile().getAbsolutePath(), jsDir.toPath(), Paths.get(jsDir.toPath().toString(), ctx.getMetaModelName() + ".js"));
                        System.out.println("done");
                        final StringBuilder sb = new StringBuilder();
                        Files.lines(javaLibJs).forEachOrdered(new Consumer<String>() {
                            @Override
                            public void accept(String line) {
                                sb.append(line).append("\n");
                            }
                        });
                        Files.lines(kmfLibJs).forEachOrdered(new Consumer<String>() {
                            @Override
                            public void accept(String line) {
                                sb.append(line).append("\n");
                            }
                        });
                        Files.lines(Paths.get(jsDir.toPath().toString(), ctx.getMetaModelName() + ".js")).forEachOrdered(new Consumer<String>() {
                            @Override
                            public void accept(String line) {
                                sb.append(line).append("\n");
                            }
                        });
                        Files.write(Paths.get(jsDir.toPath().toString(), ctx.getMetaModelName() + "-all.js"), sb.toString().getBytes());
                        tscPath.toFile().delete();
                        libDts.toFile().delete();

                        Path resourceAllJS = Paths.get(resourceOut.toPath().toString(), ctx.getMetaModelName() + "-all.js");
                        Files.copy(Paths.get(jsDir.toPath().toString(), ctx.getMetaModelName() + "-all.js"), resourceAllJS, StandardCopyOption.REPLACE_EXISTING);
                        HtmlTemplateGenerator.generateHtml(resourceOut.toPath(), ctx.getMetaModelName() + "-all.js", ctx.getMetaModelName());

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
