package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.KStringMap;
import org.kevoree.modeling.memory.map.impl.ArrayStringMap;
import org.kevoree.modeling.meta.KLiteral;
import org.kevoree.modeling.meta.KMetaEnum;
import org.kevoree.modeling.meta.MetaType;

public class MetaEnum implements KMetaEnum {

    private String _name;

    private int _index;

    private KLiteral[] _literals;

    private KStringMap<Integer> _indexes = null;

    public MetaEnum(String p_name, int p_index) {
        this._name = p_name;
        this._index = p_index;
        this._literals = new KLiteral[0];
        _indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    @Override
    public KLiteral[] literals() {
        return this._literals;
    }

    @Override
    public KLiteral literalByName(String p_name) {
        if (_indexes != null) {
            Integer resolvedIndex = _indexes.get(p_name);
            if (resolvedIndex != null) {
                return _literals[resolvedIndex];
            }
        }
        return null;
    }

    @Override
    public KLiteral literal(int p_index) {
        return this._literals[p_index];
    }

    @Override
    public KLiteral addLiteral(String p_name) {
        MetaLiteral newLiteral = new MetaLiteral(p_name, _literals.length, _name);
        internal_add_meta(newLiteral);
        return newLiteral;
    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    @Override
    public int id() {
        return _index;
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
        return MetaType.ENUM;
    }

    /**
     * @native ts
     * this._literals[p_new_meta.index()] = p_new_meta;
     * this._indexes.put(p_new_meta.metaName(), p_new_meta.index());
     */
    private void internal_add_meta(KLiteral p_new_meta) {
        KLiteral[] incArray = new KLiteral[_literals.length + 1];
        System.arraycopy(_literals, 0, incArray, 0, _literals.length);
        incArray[_literals.length] = p_new_meta;
        _literals = incArray;
        _indexes.put(p_new_meta.metaName(), p_new_meta.index());
    }

}
