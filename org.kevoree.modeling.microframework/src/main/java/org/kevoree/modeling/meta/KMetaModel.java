package org.kevoree.modeling.meta;

import org.kevoree.modeling.KModel;

public interface KMetaModel extends KMeta {

    KMetaClass[] metaClasses();

    KMetaClass metaClassByName(String name);

    KMetaClass metaClass(int index);

    KMetaClass addMetaClass(String metaClassName);

    KModel model();

}
