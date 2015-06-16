package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KType;
import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.extrapolation.impl.PolynomialExtrapolation;
import org.kevoree.modeling.memory.struct.map.KStringMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;
import org.kevoree.modeling.meta.*;

public class MetaClass implements KMetaClass {

    private String _name;

    private int _index;

    private KMeta[] _meta;

    private KStringMap<Integer> _indexes = null;

    protected MetaClass(String p_name, int p_index) {
        this._name = p_name;
        this._index = p_index;
        this._meta = new KMeta[0];
        _indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    public void init(KMeta[] p_metaElements) {
        _indexes.clear();
        _meta = p_metaElements;
        for (int i = 0; i < _meta.length; i++) {
            _indexes.put(p_metaElements[i].metaName(), p_metaElements[i].index());
        }
    }

    @Override
    public KMeta metaByName(String name) {
        if (_indexes != null) {
            Integer resolvedIndex = _indexes.get(name);
            if (resolvedIndex != null) {
                return _meta[resolvedIndex];
            }
        }
        return null;
    }

    @Override
    public KMetaAttribute attribute(String name) {
        KMeta resolved = metaByName(name);
        if (resolved != null && resolved instanceof MetaAttribute) {
            return (KMetaAttribute) resolved;
        }
        return null;
    }

    @Override
    public KMetaReference reference(String name) {
        KMeta resolved = metaByName(name);
        if (resolved != null && resolved instanceof MetaReference) {
            return (KMetaReference) resolved;
        }
        return null;
    }

    @Override
    public KMetaOperation operation(String name) {
        KMeta resolved = metaByName(name);
        if (resolved != null && resolved instanceof MetaOperation) {
            return (KMetaOperation) resolved;
        }
        return null;
    }

    @Override
    public KMeta[] metaElements() {
        return _meta;
    }

    public int index() {
        return _index;
    }

    public String metaName() {
        return _name;
    }

    @Override
    public MetaType metaType() {
        return MetaType.CLASS;
    }

    @Override
    public KMeta meta(int index) {
        if (index >= 0 && index < this._meta.length) {
            return this._meta[index];
        } else {
            return null;
        }
    }

    @Override
    public KMetaAttribute addAttribute(String attributeName, KType p_type) {
        double precisionCleaned = -1;
        Extrapolation extrapolation;
        if (p_type == KPrimitiveTypes.CONTINUOUS) {
            extrapolation = PolynomialExtrapolation.instance();
            precisionCleaned = 0.1;
        } else {
            extrapolation = DiscreteExtrapolation.instance();
        }
        KMetaAttribute tempAttribute = new MetaAttribute(attributeName, _meta.length, precisionCleaned, false, p_type, extrapolation);
        internal_add_meta(tempAttribute);
        return tempAttribute;
    }

    @Override
    public KMetaReference addReference(String referenceName, KMetaClass p_metaClass, String oppositeName, boolean toMany) {
        final KMetaClass tempOrigin = this;
        String opName = oppositeName;
        if (opName == null) {
            opName = "op_" + referenceName;
            ((MetaClass) p_metaClass).getOrCreate(opName, referenceName, this, false, false);
        } else {
            ((MetaClass) p_metaClass).getOrCreate(opName, referenceName, this, true, false);
        }
        MetaReference tempReference = new MetaReference(referenceName, _meta.length, false, !toMany, new KLazyResolver() {
            @Override
            public KMeta meta() {
                return p_metaClass;
            }
        }, opName, new KLazyResolver() {
            @Override
            public KMeta meta() {
                return tempOrigin;
            }
        });
        internal_add_meta(tempReference);
        return tempReference;
    }

    private KMetaReference getOrCreate(String p_name, String p_oppositeName, KMetaClass p_oppositeClass, boolean p_visible, boolean p_single) {
        KMetaReference previous = reference(p_name);
        if (previous != null) {
            return previous;
        }
        final KMetaClass tempOrigin = this;
        KMetaReference tempReference = new MetaReference(p_name, _meta.length, p_visible, p_single, new KLazyResolver() {
            @Override
            public KMeta meta() {
                return p_oppositeClass;
            }
        }, p_oppositeName, new KLazyResolver() {
            @Override
            public KMeta meta() {
                return tempOrigin;
            }
        });
        internal_add_meta(tempReference);
        return tempReference;
    }

    @Override
    public KMetaOperation addOperation(String operationName) {
        final KMetaClass tempOrigin = this;
        MetaOperation tempOperation = new MetaOperation(operationName, _meta.length + 1, new KLazyResolver() {
            @Override
            public KMeta meta() {
                return tempOrigin;
            }
        });
        internal_add_meta(tempOperation);
        return tempOperation;
    }

    /**
     * @native ts
     * this._meta[p_new_meta.index()] = p_new_meta;
     * this._indexes.put(p_new_meta.metaName(), p_new_meta.index());
     */
    private void internal_add_meta(KMeta p_new_meta) {
        KMeta[] incArray = new KMeta[_meta.length + 1];
        System.arraycopy(_meta, 0, incArray, 0, _meta.length);
        incArray[_meta.length] = p_new_meta;
        _meta = incArray;
        _indexes.put(p_new_meta.metaName(), p_new_meta.index());
    }

}
