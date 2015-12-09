package org.kevoree.modeling.generator.mavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.kevoree.modeling.generator.GenerationContext;
import org.kevoree.modeling.generator.Generator;
import org.kevoree.modeling.generator.JSOptimizer;
import org.kevoree.modeling.java2typescript.SourceTranslator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenModelPlugin extends AbstractMojo {

    /**
     * Ecore file
     */
    @Parameter
    private File metaModelFile;

    /**
     * code containerRoot package
     */
    @Parameter
    private String metaModelQualifiedName;

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File classesDirectory;

    /**
     * Source base directory
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/kmf")
    private File targetSrcGenDir;

    /**
     * Source base directory
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/kmf-js")
    private File jsWorkingDir;

    @Parameter(defaultValue = "false")
    private boolean js = false;

    public static final String LIB_D_TS = "lib.d.ts";
    public static final String TSC_JS = "tsc.js";

    @Parameter(defaultValue = "false")
    private boolean umd = false;

    @Parameter(defaultValue = "true")
    private boolean min = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            String targetName = metaModelFile.getName().substring(0, metaModelFile.getName().lastIndexOf("."));
            if (metaModelQualifiedName != null) {
                targetName = metaModelQualifiedName;
            }
            GenerationContext ctx = new GenerationContext();
            ctx.setMetaModel(metaModelFile);
            ctx.setMetaModelName(targetName);
            ctx.setTargetSrcDir(targetSrcGenDir);
            ctx.setVersion(project.getVersion());
            Generator generator = new Generator();
            generator.execute(ctx);
            if (js) {
                deleteRecusive(jsWorkingDir.toPath());
                Files.createDirectories(jsWorkingDir.toPath());
                Path libDts = Paths.get(jsWorkingDir.toPath().toString(), LIB_D_TS);
                Files.copy(this.getClass().getClassLoader().getResourceAsStream("tsc/" + LIB_D_TS), libDts, StandardCopyOption.REPLACE_EXISTING);
                Path tscPath = Paths.get(jsWorkingDir.toPath().toString(), TSC_JS);
                Files.copy(getClass().getClassLoader().getResourceAsStream(TSC_JS), tscPath, StandardCopyOption.REPLACE_EXISTING);
                SourceTranslator sourceTranslator = new SourceTranslator();
                sourceTranslator.additionalAppend = "microframework.browser.ts";
                sourceTranslator.exportPackage = new String[]{"org"};
                for (Artifact a : project.getDependencyArtifacts()) {
                    File file = a.getFile();
                    if (file != null) {
                        sourceTranslator.getAnalyzer().addClasspath(file.getAbsolutePath());
                        getLog().info("Add to classpath " + file.getAbsolutePath());
                    }
                }
                sourceTranslator.translateSources(targetSrcGenDir.getAbsolutePath(), jsWorkingDir.getAbsolutePath(), project.getArtifactId(), false, false, umd);
                TscRunner runner = new TscRunner();
                runner.runTsc(jsWorkingDir, classesDirectory, null, false, umd);

                //generate the min.js
                if (min) {
                    File input = new File(classesDirectory.getAbsolutePath(), project.getArtifactId() + ".js");
                    File outputMin = new File(classesDirectory.getAbsolutePath(), project.getArtifactId() + ".min.js");
                    JSOptimizer.optimize(input, outputMin);
                }

                libDts.toFile().delete();
                tscPath.toFile().delete();
            }
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("KMF Compilation error !", e);
        }
        project.addCompileSourceRoot(targetSrcGenDir.getAbsolutePath());
    }

    private void deleteRecusive(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}