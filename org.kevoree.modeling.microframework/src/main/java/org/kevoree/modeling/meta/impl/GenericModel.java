package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KOperation;
import org.kevoree.modeling.KUniverse;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;

class GenericModel extends AbstractKModel {

    private KMetaModel _p_metaModel;

    protected GenericModel(KMetaModel mm, KInternalDataManager p_manager) {
        super(p_manager);
        this._p_metaModel = mm;
    }

    @Override
    public KMetaModel metaModel() {
        return _p_metaModel;
    }

    @Override
    protected KUniverse internalCreateUniverse(long universe) {
        return new GenericUniverse(universe, _manager);
    }

    @Override
    protected KObject internalCreateObject(long universe, long time, long uuid, KMetaClass clazz, long previousUniverse, long previousTime) {
        if(clazz.index() == MetaClassIndex.INSTANCE.index()){
            return new GenericObjectIndex(universe, time, uuid, _manager, previousUniverse, previousTime);
        } else if (clazz.inferAlg() != null) {
            return new GenericObjectInfer(universe, time, uuid, clazz, _manager, previousUniverse, previousTime);
        } else {
            return new GenericObject(universe, time, uuid, clazz, _manager, previousUniverse, previousTime);
        }
    }
}
