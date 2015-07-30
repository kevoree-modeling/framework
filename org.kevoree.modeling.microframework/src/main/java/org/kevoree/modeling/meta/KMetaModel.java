package org.kevoree.modeling.meta;

import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KType;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

public interface KMetaModel extends KMeta {

    KMetaClass[] metaClasses();

    KMetaClass metaClassByName(String name);

    KMetaClass metaClass(int index);

    KMetaClass addMetaClass(String metaClassName);

    KMetaClass addInferMetaClass(String metaClassName, KInferAlg inferAlg);

    KMetaEnum[] metaTypes();

    KMetaEnum metaTypeByName(String name);

    KMetaEnum addMetaEnum(String enumName);

    KModel createModel(KInternalDataManager manager);

}
