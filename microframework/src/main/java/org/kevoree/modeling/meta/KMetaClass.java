package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.infer.KInferAlg;

public interface KMetaClass extends KMeta {

    KMeta[] metaElements();

    KMeta meta(int index);

    KMeta metaByName(String name);

    int[] metaParents();

    KMetaAttribute attribute(String name);

    KMetaRelation reference(String name);

    KMetaOperation operation(String name);

    KMetaAttribute addAttribute(String attributeName, KType p_type);

    KMetaRelation addRelation(String relationName, KMetaClass metaClass, String oppositeName);

    KMetaDependency addDependency(String dependencyName, int referredMetaClassIndex);

    KMetaInferInput addInput(String dependencyName, String extractor);

    KMetaInferOutput addOutput(String name, KType metaClass);

    KMetaOperation addOperation(String operationName);

    KInferAlg inferAlg();

    KMetaDependencies dependencies();

    KMetaInferInput[] inputs();

    KMetaInferOutput[] outputs();

    long temporalResolution();

    void setTemporalResolution(long tempo);

    void addParent(KMeta parentMetaClass);

}
