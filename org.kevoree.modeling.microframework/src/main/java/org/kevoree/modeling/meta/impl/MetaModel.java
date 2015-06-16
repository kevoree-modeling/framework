package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.memory.struct.map.KStringMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.MetaType;

public class MetaModel implements KMetaModel {

    private String _name;

    private int _index;

    private KMetaClass[] _metaClasses;

    private KStringMap<Integer> _metaClasses_indexes = null;

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
        this._metaClasses_indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    public void init(KMetaClass[] p_metaClasses) {
        _metaClasses_indexes.clear();
        _metaClasses = p_metaClasses;
        for (int i = 0; i < _metaClasses.length; i++) {
            _metaClasses_indexes.put(p_metaClasses[i].metaName(), p_metaClasses[i].index());
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
            return null;
        } else {
            return _metaClasses[resolved];
        }
    }

    @Override
    public KMetaClass metaClass(int index) {
        if (index >= 0 && index < _metaClasses.length) {
            return _metaClasses[index];
        }
        return null;
    }

    @Override
    public synchronized KMetaClass addMetaClass(String metaClassName) {
        if (_metaClasses_indexes.contains(metaClassName)) {
            return metaClassByName(metaClassName);
        } else {
            if (_metaClasses == null) {
                _metaClasses = new KMetaClass[1];
                _metaClasses[0] = new MetaClass(metaClassName, 0);
                _metaClasses_indexes.put(metaClassName, _metaClasses[0].index());
                return _metaClasses[0];
            } else {
                KMetaClass newMetaClass = new MetaClass(metaClassName, _metaClasses.length);
                interal_add_meta_class(newMetaClass);
                return newMetaClass;
            }
        }
    }

    /**
     * @native ts
     * this._metaClasses[p_newMetaClass.index()] = p_newMetaClass;
     * this._metaClasses_indexes.put(p_newMetaClass.metaName(), p_newMetaClass.index());
     */
    private void interal_add_meta_class(KMetaClass p_newMetaClass) {
        KMetaClass[] incArray = new KMetaClass[_metaClasses.length + 1];
        System.arraycopy(_metaClasses, 0, incArray, 0, _metaClasses.length);
        incArray[_metaClasses.length] = p_newMetaClass;
        _metaClasses = incArray;
        _metaClasses_indexes.put(p_newMetaClass.metaName(), p_newMetaClass.index());
    }

    @Override
    public KModel model() {
        return new GenericModel(this);
    }

}
