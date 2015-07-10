package org.kevoree.modeling.meta;

public interface KMetaDependencies extends KMeta {

    KMetaClass origin();

    KMetaDependency[] allDependencies();

    KMetaDependency dependencyByName(String dependencyName);

    KMetaDependency addDependency(String dependencyName, KMetaClass type, String oppositeName);

}
