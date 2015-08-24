package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;

public interface KMetaOperation extends KMeta {

    int originMetaClassIndex();

    int[] paramTypes();

    int returnType();

    void addParam(KType type);

    void setReturnType(KType type);
}

