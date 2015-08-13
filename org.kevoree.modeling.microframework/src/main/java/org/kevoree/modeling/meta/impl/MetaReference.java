package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.MetaType;

public class MetaReference implements KMetaReference {

    private String _name;

    private int _index;

    private boolean _visible;

    private boolean _single;

    private int _referredMetaClassIndex;

    private String _op_name;

    private int _originMetaClassIndex;

    public boolean single() {
        return _single;
    }

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
        return MetaType.REFERENCE;
    }

    @Override
    public boolean visible() {
        return _visible;
    }

    public MetaReference(String p_name, int p_index, boolean p_visible, boolean p_single, int p_referredMetaClassIndex, String op_name, int p_originMetaClassIndex) {
        this._name = p_name;
        this._index = p_index;
        this._visible = p_visible;
        this._single = p_single;
        this._referredMetaClassIndex = p_referredMetaClassIndex;
        this._op_name = op_name;
        this._originMetaClassIndex = p_originMetaClassIndex;
    }

}
