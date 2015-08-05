package org.kevoree.modeling.abs;

import org.kevoree.modeling.KType;

public class AbstractDataType implements KType {

    final private String _name;

    final private boolean _isEnum;

    final private int _id;

    public AbstractDataType(String p_name, boolean p_isEnum, int p_id) {
        this._name = p_name;
        this._isEnum = p_isEnum;
        this._id = p_id;
    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public boolean isEnum() {
        return _isEnum;
    }

    @Override
    public int id() {
        return _id;
    }

}
