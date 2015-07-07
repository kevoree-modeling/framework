package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.memory.struct.map.KStringMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;
import org.kevoree.modeling.meta.*;

public class MetaDependencies implements KMetaDependencies {

    private KLazyResolver _lazyMetaOrigin;
    private KMetaDependency[] _dependencies;
    public static final String DEPENDENCIES_NAME = "dependencies";
    private int _index;
    private KStringMap<Integer> _indexes = null;

    public MetaDependencies(int p_index, KLazyResolver p_lazyMetaOrigin) {
        this._index = p_index;
        this._lazyMetaOrigin = p_lazyMetaOrigin;
        this._dependencies = new KMetaDependency[0];
        _indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    @Override
    public KMetaClass origin() {
        if (_lazyMetaOrigin != null) {
            return (KMetaClass) _lazyMetaOrigin.meta();
        }
        return null;
    }

    @Override
    public KMetaDependency[] dependencies() {
        return _dependencies;
    }

    @Override
    public KMetaDependency dependencyByName(String dependencyName) {
        Integer foundedIndex = _indexes.get(dependencyName);
        if(foundedIndex != null){
            return _dependencies[foundedIndex];
        } else {
            return null;
        }
    }

    @Override
    public int index() {
        return this._index;
    }

    @Override
    public String metaName() {
        return DEPENDENCIES_NAME;
    }

    @Override
    public MetaType metaType() {
        return MetaType.DEPENDENCIES;
    }

    @Override
    public synchronized KMetaDependency addDependency(String p_dependencyName, KMetaClass p_type, String op_name) {
        if (op_name != null) {
            KMetaDependencies dependencies = ((MetaClass) p_type).initDependencies();
            dependencies.addDependency(op_name, origin(), null);
        }
        KMetaDependency newDependency = new MetaDependency(p_dependencyName, _dependencies.length, this, new KLazyResolver() {
            @Override
            public KMeta meta() {
                return p_type;
            }
        }, op_name);
        internal_add_dep(newDependency);
        return newDependency;
    }

    /**
     * @native ts
     * this._dependencies[p_new_meta.index()] = p_new_meta;
     * this._indexes.put(p_new_meta.metaName(), p_new_meta.index());
     */
    private void internal_add_dep(KMetaDependency p_new_meta) {
        KMetaDependency[] incArray = new KMetaDependency[_dependencies.length + 1];
        System.arraycopy(_dependencies, 0, incArray, 0, _dependencies.length);
        incArray[_dependencies.length] = p_new_meta;
        _dependencies = incArray;
        _indexes.put(p_new_meta.metaName(), p_new_meta.index());
    }

}
