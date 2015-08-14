package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.infer.KInferAlg;

public interface KMetaClass extends KMeta {

    KMeta[] metaElements();

    KMeta meta(int index);

    KMeta metaByName(String name);

    KMetaAttribute attribute(String name);

    KMetaReference reference(String name);

    KMetaOperation operation(String name);

    KMetaAttribute addAttribute(String attributeName, KType p_type);

    KMetaReference addReference(String referenceName, KMetaClass metaClass, String oppositeName, boolean toMany);

    KMetaDependency addDependency(String dependencyName, int referredMetaClassIndex);

    KMetaInferInput addInput(String name, String extractor);

    KMetaInferOutput addOutput(String name, KType metaClass);

    KMetaOperation addOperation(String operationName);

    KInferAlg inferAlg();

    KMetaDependencies dependencies();

    KMetaInferInput[] inputs();

    KMetaInferOutput[] outputs();

    long temporalResolution();

    void setTemporalResolution(long tempo);

}
