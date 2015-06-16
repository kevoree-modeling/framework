package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.extrapolation.Extrapolation;

public interface KMetaClass extends KMeta {

    KMeta[] metaElements();

    KMeta meta(int index);

    KMeta metaByName(String name);

    KMetaAttribute attribute(String name);

    KMetaReference reference(String name);

    KMetaOperation operation(String name);

    KMetaAttribute addAttribute(String attributeName, KType p_type);

    KMetaReference addReference(String referenceName, KMetaClass metaClass, String oppositeName, boolean toMany);

    KMetaOperation addOperation(String operationName);

}
