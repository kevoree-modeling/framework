package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.MetaType;

public class MetaOperation implements KMetaOperation {

    private String _name;

    private int _index;

    private KLazyResolver _lazyMetaClass;

    @Override
    public int index() {
        return _index;
    }

    @Override
    public String metaName() {
        return _name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.OPERATION;
    }

    public MetaOperation(String p_name, int p_index, KLazyResolver p_lazyMetaClass) {
        this._name = p_name;
        this._index = p_index;
        this._lazyMetaClass = p_lazyMetaClass;
    }

    @Override
    public KMetaClass origin() {
        if(_lazyMetaClass!=null){
            return (KMetaClass) _lazyMetaClass.meta();
        }
        return null;
    }
}
