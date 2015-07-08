package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.meta.*;

public class MetaDependency implements KMetaDependency {

    private String _name;

    private int _index;

    private KMetaDependencies _origin;

    private KLazyResolver _lazyMetaType;

    private String _oppositeName;

    public MetaDependency(String p_name, int p_index, KMetaDependencies p_origin, KLazyResolver p_lazyMetaType, String p_oppositeName) {
        this._name = p_name;
        this._index = p_index;
        this._origin = p_origin;
        this._lazyMetaType = p_lazyMetaType;
        this._oppositeName = p_oppositeName;
    }

    public KMetaClass type() {
        if (_lazyMetaType != null) {
            return (KMetaClass) _lazyMetaType.meta();
        } else {
            return null;
        }
    }

    @Override
    public KMetaDependency opposite() {
        if (_oppositeName != null) {
            KMetaDependencies dependencies = type().dependencies();
            if(dependencies != null){
                return dependencies.dependencyByName(_oppositeName);
            }
        }
        return null;
    }

    @Override
    public KMetaDependencies origin() {
        return this._origin;
    }

    @Override
    public int index() {
        return this._index;
    }

    @Override
    public String metaName() {
        return _name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.DEPENDENCY;
    }

}
