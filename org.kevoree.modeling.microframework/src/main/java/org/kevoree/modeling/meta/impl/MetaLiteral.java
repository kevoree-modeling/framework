package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.meta.KLiteral;
import org.kevoree.modeling.meta.MetaType;

public class MetaLiteral implements KLiteral {

    private String _name;

    private int _index;

    private String _className;

    public MetaLiteral(String p_name, int p_index, String p_className) {
        this._name = p_name;
        this._index = p_index;
        this._className = p_className;
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
    public String toString() {
        return "KLiteral@" + this._className + "." + this._name;
    }

}
