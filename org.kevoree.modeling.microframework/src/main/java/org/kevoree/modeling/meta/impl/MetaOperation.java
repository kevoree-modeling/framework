package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.MetaType;

public class MetaOperation implements KMetaOperation {

    private String _name;

    private int _index;

    private int _originMetaClassIndex;

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

    @Override
    public int originMetaClassIndex() {
        return _originMetaClassIndex;
    }

    public MetaOperation(String p_name, int p_index, int p_originMetaClassIndex) {
        this._name = p_name;
        this._index = p_index;
        this._originMetaClassIndex = p_originMetaClassIndex;
    }

}
