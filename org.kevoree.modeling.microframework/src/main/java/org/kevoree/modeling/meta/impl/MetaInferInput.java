package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KMetaInferInput;
import org.kevoree.modeling.meta.MetaType;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.query.impl.QueryEngine;

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
    public String extractorQuery() {
        return this._extractor;
    }

    private KTraversal _cachedTraversal;

    @Override
    public KTraversal extractor() {
        if(_cachedTraversal != null){
            return this._cachedTraversal;
        } else {
            return cacheTraversal();
        }
    }

    private synchronized KTraversal cacheTraversal(){
        this._cachedTraversal = QueryEngine.getINSTANCE().buildTraversal(this._extractor);
        return this._cachedTraversal;
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
