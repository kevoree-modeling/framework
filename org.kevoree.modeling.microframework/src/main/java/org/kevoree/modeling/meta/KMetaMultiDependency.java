package org.kevoree.modeling.meta;

public interface KMetaMultiDependency extends KMeta {

    boolean single();

    KMetaClass type();

    KMetaReference opposite();

    KMetaClass origin();

}
