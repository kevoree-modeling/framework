package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.*;

public class MetaDependency implements KMetaDependency {

    private String _name;

    private int _index;

    private int _referredMetaClassIndex;

    @Override
    public int referredMetaClassIndex() {
        return _referredMetaClassIndex;
    }

    public int index() {
        return _index;
    }

    public String metaName() {
        return _name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.DEPENDENCY;
    }

    public MetaDependency(String p_name, int p_index, KMetaDependencies p_origin, int p_referredMetaClassIndex) {
        this._name = p_name;
        this._index = p_index;
        this._referredMetaClassIndex = p_referredMetaClassIndex;
    }

}
