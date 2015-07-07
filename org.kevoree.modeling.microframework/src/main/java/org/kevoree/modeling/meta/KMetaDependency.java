package org.kevoree.modeling.meta;

public interface KMetaDependency extends KMeta {

    KMetaClass type();

    KMetaDependency opposite();

    KMetaDependencies origin();

}
