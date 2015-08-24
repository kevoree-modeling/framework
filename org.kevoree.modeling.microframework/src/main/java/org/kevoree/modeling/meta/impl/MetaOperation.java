package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.MetaType;

public class MetaOperation implements KMetaOperation {

    private String _name;

    private int _index;

    private int _originMetaClassIndex;

    private int[] _paramTypes = null;

    private int _returnType;

    @Override
    public int index() {
        return _index;
    }

    @Override
    public String metaName() {
        return _name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.OPERATION;
    }

    @Override
    public int originMetaClassIndex() {
        return _originMetaClassIndex;
    }

    @Override
    public int[] paramTypes() {
        return this._paramTypes;
    }

    @Override
    public int returnType() {
        return this._returnType;
    }

    public MetaOperation(String p_name, int p_index, int p_originMetaClassIndex, int[] p_paramTypes, int p_returnType) {
        this._name = p_name;
        this._index = p_index;
        this._originMetaClassIndex = p_originMetaClassIndex;
        this._paramTypes = p_paramTypes;
        this._returnType = p_returnType;
    }

    @Override
    public void addParam(KType type) {
        int[] newParam = new int[_paramTypes.length + 1];
        System.arraycopy(_paramTypes, 0, newParam, 0, _paramTypes.length);
        newParam[_paramTypes.length] = type.id();
        this._paramTypes = newParam;
    }

    @Override
    public void setReturnType(KType type) {
        this._returnType = type.id();
    }

}
