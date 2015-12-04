package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;

public interface KMetaOperation extends KMeta {

    int originMetaClassIndex();

    int[] paramTypes();

    boolean[] paramMultiplicities();

    int returnType();

    boolean returnTypeIsArray();

    void addParam(KType type, boolean isArray);

    void setReturnType(KType type, boolean isArray);
}

