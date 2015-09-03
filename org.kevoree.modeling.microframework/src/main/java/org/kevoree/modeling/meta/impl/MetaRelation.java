package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KMetaRelation;
import org.kevoree.modeling.meta.MetaType;

public class MetaRelation implements KMetaRelation {

    private String _name;

    private int _index;

    private boolean _visible;

    private int _referredMetaClassIndex;

    private String _op_name;

    private int _originMetaClassIndex;

    private int _maxBound = -1;

    @Override
    public int referredMetaClassIndex() {
        return _referredMetaClassIndex;
    }

    @Override
    public String oppositeName() {
        return _op_name;
    }

    @Override
    public int originMetaClassIndex() {
        return _originMetaClassIndex;
    }

    public int index() {
        return _index;
    }

    public String metaName() {
        return _name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.RELATION;
    }

    @Override
    public boolean visible() {
        return _visible;
    }

    @Override
    public int maxBound() {
        return this._maxBound;
    }

    @Override
    public void setMaxBound(int p_maxBound) {
        this._maxBound = p_maxBound;
    }

    public MetaRelation(String p_name, int p_index, boolean p_visible, int p_referredMetaClassIndex, String op_name, int p_originMetaClassIndex, int p_maxBound) {
        this._name = p_name;
        this._index = p_index;
        this._visible = p_visible;
        this._referredMetaClassIndex = p_referredMetaClassIndex;
        this._op_name = op_name;
        this._originMetaClassIndex = p_originMetaClassIndex;
        this._maxBound = p_maxBound;
    }

}
