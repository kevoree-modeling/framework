package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KMetaInferInput;
import org.kevoree.modeling.meta.MetaType;

public class MetaInferInput implements KMetaInferInput {

    private String _name;

    private int _index;

    private String _extractor;

    public MetaInferInput(String p_name, int p_index, String p_extractor) {
        this._name = p_name;
        this._index = p_index;
        this._extractor = p_extractor;
    }

    @Override
    public String extractor() {
        return this._extractor;
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
        return MetaType.INPUT;
    }
}
