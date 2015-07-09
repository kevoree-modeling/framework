package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.meta.*;

public class MetaInferOutput implements KMetaInferOutput {

    private String _name;

    private int _index;

    private KType _type;

    public MetaInferOutput(String p_name, int p_index, KType p_type) {
        this._name = p_name;
        this._index = p_index;
        this._type = p_type;
    }

    @Override
    public int index() {
        return this._index;
    }

    @Override
    public String metaName() {
        return this._name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.OUTPUT;
    }

    @Override
    public KType type() {
        return this._type;
    }

}
