package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KStringMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.util.PrimitiveHelper;

public class MetaModel implements KMetaModel {

    private String _name;

    private int _index;

    private KMetaClass[] _metaClasses;

    private KStringMap<Integer> _metaClasses_indexes = null;

    private KMetaEnum[] _metaTypes;

    private KStringMap<Integer> _metaTypes_indexes = null;

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
        return MetaType.MODEL;
    }

    public MetaModel(String p_name) {
        this._name = p_name;
        this._index = 0;
        this._metaClasses = new KMetaClass[0];
        this._metaTypes = new KMetaEnum[0];
        this._metaClasses_indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._metaTypes_indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    public void init(KMetaClass[] p_metaClasses, KMetaEnum[] p_metaEnums) {
        _metaClasses_indexes.clear();
        _metaTypes_indexes.clear();
        _metaClasses = p_metaClasses;
        _metaTypes = p_metaEnums;
        for (int i = 0; i < _metaClasses.length; i++) {
            _metaClasses_indexes.put(p_metaClasses[i].metaName(), p_metaClasses[i].index());
        }
        for (int i = 0; i < _metaTypes.length; i++) {
            _metaTypes_indexes.put(p_metaEnums[i].metaName(), p_metaEnums[i].index());
        }
    }

    @Override
    public KMetaClass[] metaClasses() {
        return _metaClasses;
    }

    @Override
    public KMetaClass metaClassByName(String name) {
        if (_metaClasses_indexes == null) {
            return null;
        }
        Integer resolved = _metaClasses_indexes.get(name);
        if (resolved == null) {
            if(PrimitiveHelper.equals(name,MetaClassIndex.INSTANCE.metaName())){
                return MetaClassIndex.INSTANCE;
            } else {
                return null;
            }
        } else {
            return _metaClasses[resolved];
        }
    }

    @Override
    public KMetaClass metaClass(int index) {
        if(index == MetaClassIndex.INSTANCE.index()){
            return MetaClassIndex.INSTANCE;
        }
        if (index >= 0 && index < _metaClasses.length) {
            return _metaClasses[index];
        }
        return null;
    }

    @Override
    public KMetaClass addMetaClass(String metaClassName) {
        return internal_addmetaclass(metaClassName, null);
    }

    @Override
    public KMetaClass addInferMetaClass(String metaClassName, KInferAlg inferAlg) {
        return internal_addmetaclass(metaClassName, inferAlg);
    }

    @Override
    public KMetaEnum[] metaTypes() {
        return this._metaTypes;
    }

    @Override
    public KMetaEnum metaTypeByName(String p_name) {
        if (this._metaTypes == null) {
            return null;
        }
        Integer resolved = this._metaTypes_indexes.get(p_name);
        if (resolved == null) {
            return null;
        } else {
            return _metaTypes[resolved];
        }
    }

    @Override
    public KMetaEnum addMetaEnum(String enumName) {
        KMetaEnum newEnumType = new MetaEnum(enumName, _metaTypes.length);
        internal_add_type(newEnumType);
        return newEnumType;
    }

    private synchronized KMetaClass internal_addmetaclass(String metaClassName, KInferAlg alg) {
        if (_metaClasses_indexes.contains(metaClassName)) {
            return metaClassByName(metaClassName);
        } else {
            if (_metaClasses == null) {
                _metaClasses = new KMetaClass[1];
                _metaClasses[0] = new MetaClass(metaClassName, 0, alg, new int[]{});
                _metaClasses_indexes.put(metaClassName, _metaClasses[0].index());
                return _metaClasses[0];
            } else {
                KMetaClass newMetaClass = new MetaClass(metaClassName, _metaClasses.length, alg, new int[]{});
                internal_add_meta_class(newMetaClass);
                return newMetaClass;
            }
        }
    }

    /**
     * @native ts
     * this._metaClasses[p_newMetaClass.index()] = p_newMetaClass;
     * this._metaClasses_indexes.put(p_newMetaClass.metaName(), p_newMetaClass.index());
     */
    private void internal_add_meta_class(KMetaClass p_newMetaClass) {
        KMetaClass[] incArray = new KMetaClass[_metaClasses.length + 1];
        System.arraycopy(_metaClasses, 0, incArray, 0, _metaClasses.length);
        incArray[_metaClasses.length] = p_newMetaClass;
        _metaClasses = incArray;
        _metaClasses_indexes.put(p_newMetaClass.metaName(), p_newMetaClass.index());
    }

    /**
     * @native ts
     * this._metaTypes[p_newType.index()] = p_newType;
     * this._metaTypes_indexes.put(p_newType.metaName(), p_newType.index());
     */
    private void internal_add_type(KMetaEnum p_newType) {
        KMetaEnum[] incArray = new KMetaEnum[_metaTypes.length + 1];
        System.arraycopy(_metaTypes, 0, incArray, 0, _metaTypes.length);
        incArray[_metaTypes.length] = p_newType;
        _metaTypes = incArray;
        _metaTypes_indexes.put(p_newType.name(), p_newType.index());
    }

    @Override
    public KModel createModel(KInternalDataManager p_manager) {
        return new GenericModel(this, p_manager);
    }

}
