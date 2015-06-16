package org.kevoree.modeling.meta;

public interface KMetaReference extends KMeta {

    boolean visible();

    boolean single();

    KMetaClass type();

    KMetaReference opposite();

    KMetaClass origin();

}
