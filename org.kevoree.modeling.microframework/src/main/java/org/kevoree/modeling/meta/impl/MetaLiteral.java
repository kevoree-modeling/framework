package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KLiteral;
import org.kevoree.modeling.meta.KMetaEnum;
import org.kevoree.modeling.meta.MetaType;

public class MetaLiteral implements KLiteral {

    private String _name;

    private int _index;

    private KMetaEnum _origin;

    public MetaLiteral(String p_name, int p_index, KMetaEnum p_origin) {
        this._name = p_name;
        this._index = p_index;
        this._origin = p_origin;
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
        return MetaType.LITERAL;
    }

    @Override
    public KMetaEnum origin() {
        return this._origin;
    }

    @Override
    public String toString(){
        return "KLiteral@"+this._origin.name()+"."+this._name;
    }

}
