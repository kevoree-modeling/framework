package org.kevoree.modeling.meta;

public interface KMetaReference extends KMeta {

    boolean visible();

    boolean single();

    int referredMetaClassIndex();

    String oppositeName();

    int originMetaClassIndex();

}
