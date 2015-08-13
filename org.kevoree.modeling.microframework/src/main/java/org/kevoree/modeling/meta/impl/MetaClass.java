package org.kevoree.modeling.meta.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KType;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.extrapolation.impl.PolynomialExtrapolation;
import org.kevoree.modeling.infer.KInferAlg;
import org.kevoree.modeling.memory.chunk.KStringMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;
import org.kevoree.modeling.meta.*;

public class MetaClass implements KMetaClass {

    private String _name;

    private int _index;

    private KMeta[] _meta;

    private KStringMap<Integer> _indexes = null;

    private KInferAlg _alg;

    private KMetaInferInput[] _cachedInputs = null;

    private KMetaInferOutput[] _cachedOutputs = null;

    private long _temporalResolution = 1;

    protected MetaClass(String p_name, int p_index, KInferAlg p_alg) {
        this._name = p_name;
        this._index = p_index;
        this._meta = new KMeta[0];
        this._alg = p_alg;
        _indexes = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        if (this._alg != null) {
            //in case of inference algorithm, we always inject a multi dependency object
            internal_add_meta(new MetaDependencies(_meta.length, this));
        }
    }

    public void init(KMeta[] p_metaElements) {
        _indexes.clear();
        _meta = p_metaElements;
        for (int i = 0; i < _meta.length; i++) {
            _indexes.put(p_metaElements[i].metaName(), p_metaElements[i].index());
        }
        clearCached();
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
        if (resolved != null && resolved.metaType() == MetaType.ATTRIBUTE) {
            return (KMetaAttribute) resolved;
        }
        return null;
    }

    @Override
    public KMetaReference reference(String name) {
        KMeta resolved = metaByName(name);
        if (resolved != null && resolved.metaType() == MetaType.REFERENCE) {
            return (KMetaReference) resolved;
        }
        return null;
    }

    @Override
    public KMetaOperation operation(String name) {
        KMeta resolved = metaByName(name);
        if (resolved != null && resolved.metaType() == MetaType.OUTPUT) {
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
        return internal_addatt(attributeName, p_type);
    }

    private KMetaAttribute internal_addatt(String attributeName, KType p_type) {
        double precisionCleaned = -1;
        Extrapolation extrapolation;
        if (p_type.id() == KPrimitiveTypes.CONTINUOUS_ID) {
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
        return internal_addref(referenceName, p_metaClass, oppositeName, toMany);
    }

    private KMetaReference internal_addref(String referenceName, KMetaClass p_metaClass, String oppositeName, boolean toMany) {
        final KMetaClass tempOrigin = this;
        String opName = oppositeName;
        if (opName == null) {
            opName = "op_" + referenceName;
            ((MetaClass) p_metaClass).getOrCreate(opName, referenceName, this, false, false);
        } else {
            ((MetaClass) p_metaClass).getOrCreate(opName, referenceName, this, true, false);
        }
        MetaReference tempReference = new MetaReference(referenceName, _meta.length, false, !toMany, p_metaClass.index(), opName, tempOrigin.index());
        internal_add_meta(tempReference);
        return tempReference;
    }

    private KMetaReference getOrCreate(String p_name, String p_oppositeName, KMetaClass p_oppositeClass, boolean p_visible, boolean p_single) {
        KMetaReference previous = reference(p_name);
        if (previous != null) {
            return previous;
        }
        final KMetaClass tempOrigin = this;
        KMetaReference tempReference = new MetaReference(p_name, _meta.length, p_visible, p_single, p_oppositeClass.index(), p_oppositeName, tempOrigin.index());
        internal_add_meta(tempReference);
        return tempReference;
    }

    @Override
    public KMetaOperation addOperation(String operationName) {
        final KMetaClass tempOrigin = this;
        MetaOperation tempOperation = new MetaOperation(operationName, _meta.length, tempOrigin.index());
        internal_add_meta(tempOperation);
        return tempOperation;
    }

    @Override
    public KInferAlg inferAlg() {
        return _alg;
    }

    @Override
    public KMetaDependency addDependency(String dependencyName, KMetaClass p_metaClass) {
        KMetaDependencies currentDeps = dependencies();
        if (currentDeps != null) {
            return currentDeps.addDependency(dependencyName, p_metaClass);
        }
        return null;
    }

    @Override
    public KMetaInferInput addInput(String p_name, String p_extractor) {
        KMetaInferInput newInput = new MetaInferInput(p_name, _meta.length, p_extractor);
        internal_add_meta(newInput);
        return newInput;
    }

    @Override
    public KMetaInferOutput addOutput(String p_name, KType p_type) {
        KMetaInferOutput newOutput = new MetaInferOutput(p_name, _meta.length, p_type);
        internal_add_meta(newOutput);
        return newOutput;
    }

    @Override
    public KMetaDependencies dependencies() {
        return (KMetaDependencies) metaByName(MetaDependencies.DEPENDENCIES_NAME);
    }

    @Override
    public KMetaInferInput[] inputs() {
        if (_cachedInputs == null) {
            cacheInputs();
        }
        return this._cachedInputs;
    }

    private synchronized void cacheInputs() {
        int nb = 0;
        for (int i = 0; i < _meta.length; i++) {
            if (_meta[i].metaType().equals(MetaType.INPUT)) {
                nb++;
            }
        }
        this._cachedInputs = new KMetaInferInput[nb];
        nb = 0;
        for (int i = 0; i < _meta.length; i++) {
            if (_meta[i].metaType().equals(MetaType.INPUT)) {
                this._cachedInputs[nb] = (KMetaInferInput) _meta[i];
                nb++;
            }
        }
    }


    @Override
    public KMetaInferOutput[] outputs() {
        if (_cachedOutputs == null) {
            cacheOuputs();
        }
        return _cachedOutputs;
    }

    @Override
    public long temporalResolution() {
        return _temporalResolution;
    }

    @Override
    public void setTemporalResolution(long p_tempo) {
        this._temporalResolution = p_tempo;
    }

    private synchronized void cacheOuputs() {
        int nb = 0;
        for (int i = 0; i < _meta.length; i++) {
            if (_meta[i].metaType().equals(MetaType.OUTPUT)) {
                nb++;
            }
        }
        this._cachedOutputs = new KMetaInferOutput[nb];
        nb = 0;
        for (int i = 0; i < _meta.length; i++) {
            if (_meta[i].metaType().equals(MetaType.OUTPUT)) {
                this._cachedOutputs[nb] = (KMetaInferOutput) _meta[i];
                nb++;
            }
        }
    }

    private void clearCached() {
        this._cachedOutputs = null;
        this._cachedInputs = null;
    }

    /**
     * @native ts
     * this.clearCached();
     * this._meta[p_new_meta.index()] = p_new_meta;
     * this._indexes.put(p_new_meta.metaName(), p_new_meta.index());
     */
    private void internal_add_meta(KMeta p_new_meta) {
        clearCached();
        KMeta[] incArray = new KMeta[_meta.length + 1];
        System.arraycopy(_meta, 0, incArray, 0, _meta.length);
        incArray[_meta.length] = p_new_meta;
        _meta = incArray;
        _indexes.put(p_new_meta.metaName(), p_new_meta.index());
    }

}
