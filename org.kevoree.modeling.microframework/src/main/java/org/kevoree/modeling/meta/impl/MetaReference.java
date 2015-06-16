package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.MetaType;

public class MetaReference implements KMetaReference {

    private String _name;

    private int _index;

    private boolean _visible;

    private boolean _single;

    private KLazyResolver _lazyMetaType;

    private String _op_name;

    private KLazyResolver _lazyMetaOrigin;

    public boolean single() {
        return _single;
    }

    public KMetaClass type() {
        if (_lazyMetaType != null) {
            return (KMetaClass) _lazyMetaType.meta();
        } else {
            return null;
        }
    }

    public KMetaReference opposite() {
        if (_op_name != null) {
            return type().reference(_op_name);
        }
        return null;
    }

    @Override
    public KMetaClass origin() {
        if (_lazyMetaOrigin != null) {
            return (KMetaClass) _lazyMetaOrigin.meta();
        }
        return null;
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

    public MetaReference(String p_name, int p_index, boolean p_visible, boolean p_single, KLazyResolver p_lazyMetaType, String op_name, KLazyResolver p_lazyMetaOrigin) {
        this._name = p_name;
        this._index = p_index;
        this._visible = p_visible;
        this._single = p_single;
        this._lazyMetaType = p_lazyMetaType;
        this._op_name = op_name;
        this._lazyMetaOrigin = p_lazyMetaOrigin;
    }

}
