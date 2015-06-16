package org.kevoree.modeling.abs;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.meta.KPrimitiveTypes;

public class AbstractDataType implements KType {

    final private String _name;

    final private boolean _isEnum;

    public AbstractDataType(String p_name, boolean p_isEnum) {
        this._name = p_name;
        this._isEnum = p_isEnum;
    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public boolean isEnum() {
        return _isEnum;
    }

}
