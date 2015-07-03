package org.kevoree.modeling.meta;

public interface KMetaDependency extends KMeta {

    boolean single();

    KMetaClass type();

    KMetaReference opposite();

    KMetaClass origin();

    String extractor();

}
