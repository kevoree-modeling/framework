package org.kevoree.modeling.generator;

import org.kevoree.modeling.ast.MModel;
import org.kevoree.modeling.ast.MModelClassifier;

import java.io.File;
import java.util.HashMap;

public class GenerationContext {

    private File metaModel;
    private String metaModelName;
    private String metaModelPackage;
    private String version;
    public File targetSrcDir;

    public String getMetaModelPackage() {
        return metaModelPackage;
    }

    public File getMetaModel() {
        return metaModel;
    }

    public void setMetaModel(File metaModel) {
        this.metaModel = metaModel;
    }

    public String getMetaModelName() {
        return metaModelName;
    }

    public void setMetaModelName(String metaModelName) {
        if (metaModelName.contains(".")) {
            this.metaModelPackage = metaModelName.substring(0, metaModelName.lastIndexOf("."));
            this.metaModelName = toCamelCase(metaModelName.substring(metaModelName.lastIndexOf(".") + 1));
        } else {
            this.metaModelName = toCamelCase(metaModelName);
            this.metaModelPackage = metaModelName.toLowerCase();
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getTargetSrcDir() {
        return targetSrcDir;
    }

    public void setTargetSrcDir(File targetSrcDir) {
        this.targetSrcDir = targetSrcDir;
    }

    /* GENERATION DATA */
    public ProcessorHelper helper = ProcessorHelper.getInstance();

    public ProcessorHelper getHelper() {
        return helper;
    }

    private MModel model = new MModel();

    public MModel getModel() {
        return model;
    }

    private String toCamelCase(String other) {
        return other.substring(0,1).toUpperCase() + other.substring(1);
    }
}