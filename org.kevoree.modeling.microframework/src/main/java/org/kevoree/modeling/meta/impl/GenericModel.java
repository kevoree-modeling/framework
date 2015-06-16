package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KUniverse;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;

class GenericModel extends AbstractKModel {

    private KMetaModel _p_metaModel;

    protected GenericModel(KMetaModel mm) {
        super();
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
    protected KObject internalCreateObject(long universe, long time, long uuid, KMetaClass clazz) {
        return new GenericObject(universe, time, uuid, clazz, _manager);
    }
}
