package org.kevoree.modeling.generator;

import org.kevoree.modeling.ast.MModelClassifier;

public class ClassGenerationContext {

    public GenerationContext generationContext;
    //public String classPackage, classFqn, className;
    public MModelClassifier classDeclaration;

    public GenerationContext getGenerationContext() {
        return generationContext;
    }

    public MModelClassifier getClassDeclaration() {
        return classDeclaration;
    }
}
