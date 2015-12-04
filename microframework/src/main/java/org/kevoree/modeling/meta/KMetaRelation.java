package org.kevoree.modeling.meta;

public interface KMetaRelation extends KMeta {

    int originMetaClassIndex();

    int referredMetaClassIndex();

    String oppositeName();

    boolean visible();

    int maxBound();

    void setMaxBound(int bound);

}
