package org.kevoree.modeling.generator;

import org.kevoree.modeling.ast.MModelClassifier;

/**
 * Created by gregory.nain on 14/10/2014.
 */
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
