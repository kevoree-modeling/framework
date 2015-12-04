package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.MetaType;

public class MetaOperation implements KMetaOperation {

    private String _name;

    private int _index;

    private int _originMetaClassIndex;

    private int[] _paramTypes = null;

    private boolean[] _paramIsArray = null;

    private int _returnType;

    private boolean _returnIsArray = false;

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
    public boolean[] paramMultiplicities() {
        return this._paramIsArray;
    }

    @Override
    public int returnType() {
        return this._returnType;
    }

    @Override
    public boolean returnTypeIsArray() {
        return _returnIsArray;
    }

    public MetaOperation(String p_name, int p_index, int p_originMetaClassIndex, int[] p_paramTypes, int p_returnType, boolean[] p_paramIsArray, boolean p_returnIsArray) {
        this._name = p_name;
        this._index = p_index;
        this._originMetaClassIndex = p_originMetaClassIndex;
        this._paramTypes = p_paramTypes;
        this._returnType = p_returnType;
        this._paramIsArray = p_paramIsArray;
        this._returnIsArray = p_returnIsArray;
    }

    @Override
    public void addParam(KType type, boolean isArray) {

        int[] newParam = new int[_paramTypes.length + 1];
        boolean[] newParamIsArray = new boolean[_paramIsArray.length + 1];

        System.arraycopy(_paramTypes, 0, newParam, 0, _paramTypes.length);
        System.arraycopy(_paramIsArray, 0, newParamIsArray, 0, _paramIsArray.length);

        newParam[_paramTypes.length] = type.id();
        newParamIsArray[_paramIsArray.length] = isArray;

        this._paramTypes = newParam;
        this._paramIsArray = newParamIsArray;
    }

    @Override
    public void setReturnType(KType type, boolean isArray) {
        this._returnType = type.id();
        this._returnIsArray = isArray;
    }

}
