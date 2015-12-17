package org.kevoree.modeling.abs;

import org.kevoree.modeling.*;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.chunk.KStringMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.KOperation;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.defer.impl.Defer;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.impl.Traversal;
import org.kevoree.modeling.traversal.query.impl.QueryEngine;
import org.kevoree.modeling.util.Checker;
import org.kevoree.modeling.util.PrimitiveHelper;

public abstract class AbstractKModel<A extends KUniverse> implements KModel<A> {

    final protected KInternalDataManager _manager;

    final private long _key;

    protected AbstractKModel(KInternalDataManager p_manager) {
        _manager = p_manager;
        _manager.setModel(this);
        _key = _manager.nextModelKey();
    }

    public abstract KMetaModel metaModel();

    @Override
    public void connect(KCallback cb) {
        _manager.connect(cb);
    }

    @Override
    public void disconnect(KCallback cb) {
        _manager.close(cb);
    }

    @Override
    public KDataManager manager() {
        return _manager;
    }

    @Override
    public A newUniverse() {
        long nextKey = _manager.nextUniverseKey();
        final A newDimension = internalCreateUniverse(nextKey);
        _manager.initUniverse(nextKey, nextKey);
        return newDimension;
    }

    protected abstract A internalCreateUniverse(long universe);

    protected abstract KObject internalCreateObject(long universe, long time, long uuid, KMetaClass clazz, long previousUniverse, long previousTime);

    //TODO drop the metaClass param, replace by index
    public KObject createProxy(long universe, long time, long uuid, KMetaClass clazz, long previousUniverse, long previousTime) {
        return internalCreateObject(universe, time, uuid, clazz, previousUniverse, previousTime);
    }

    @Override
    public A universe(long key) {
        A newDimension = internalCreateUniverse(key);
        _manager.initUniverse(key, key);
        return newDimension;
    }

    @Override
    public void save(KCallback callback) {
        _manager.save(callback);
    }

    @Override
    public void setOperation(KMetaOperation metaOperation, KOperation operation) {
        _manager.operationManager().register(metaOperation, operation);
    }

    @Override
    public void setOperationByName(String metaClassName, String metaOperationName, KOperation operation) {
        KMetaClass tempMC = _manager.model().metaModel().metaClassByName(metaClassName);
        if (tempMC != null) {
            KMetaOperation tempMO = tempMC.operation(metaOperationName);
            if (tempMO != null) {
                _manager.operationManager().register(tempMO, operation);
            } else {
                throw new RuntimeException("MetaOperation not found with name " + metaOperationName + " on MetaClass " + metaClassName);
            }
        } else {
            throw new RuntimeException("MetaClass not found with name " + metaClassName);
        }
    }

    @Override
    public KDefer defer() {
        return new Defer();
    }

    @Override
    public long key() {
        return this._key;
    }

    @Override
    public KObject create(KMetaClass clazz, long universe, long time) {
        if (!Checker.isDefined(clazz)) {
            return null;
        }
        KObject newObj = internalCreateObject(universe, time, _manager.nextObjectKey(), clazz, universe, time);
        if (newObj != null) {
            _manager.initKObject(newObj);
        }
        return newObj;
    }

    @Override
    public KObject createByName(String metaClassName, long universe, long time) {
        return create(_manager.model().metaModel().metaClassByName(metaClassName), universe, time);
    }

    @Override
    public void lookup(long p_universe, long p_time, long p_uuid, KCallback<KObject> callback) {
        _manager.lookup(p_universe, p_time, p_uuid, callback);
    }

    @Override
    public void lookupAllObjects(long p_universe, long p_time, long[] p_uuids, KCallback<KObject[]> callback) {
        _manager.lookupAllObjects(p_universe, p_time, p_uuids, callback);
    }

    @Override
    public void lookupAllTimes(long p_universe, long[] p_times, long p_uuids, KCallback<KObject[]> callback) {
        _manager.lookupAllTimes(p_universe, p_times, p_uuids, callback);
    }

    @Override
    public KPreparedLookup createPreparedLookup(int p_size) {
        return _manager.createPreparedLookup(p_size);
    }

    @Override
    public void lookupPrepared(KPreparedLookup p_prepared, KCallback<KObject[]> p_callback) {
        _manager.lookupPrepared(p_prepared, p_callback);
    }

    @Override
    public KListener createListener(long universe) {
        return _manager.createListener(universe);
    }

    @Override
    public KModelContext createModelContext() {
        return new AbstractKModelContext(this);
    }

    @Override
    public KTraversal createTraversal(KObject[] startingElements) {
        return new Traversal(startingElements);
    }

    @Override
    public KTraversal createReusableTraversal() {
        return new Traversal(null);
    }

    @Override
    public void find(KMetaClass metaClass, long universe, long time, String attributes, KCallback<KObject> callback) {
        findByName(metaClass.metaName(), universe, time, attributes, callback);
    }

    @Override
    public void findByName(String indexName, long universe, long time, String attributes, KCallback<KObject> callback) {
        if (!Checker.isDefined(attributes)) {
            if (Checker.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            _manager.index(universe, time, indexName, false, new KCallback<KObjectIndex>() {
                @Override
                public void on(KObjectIndex kObjectIndex) {
                    String concat = "";
                    KStringMap<String> params = buildParams(attributes);
                    if (params.size() == 0) {
                        concat = attributes;
                    } else {
                        KMetaClass currentClass = metaModel().metaClassByName(indexName);
                        if (currentClass == null) {
                            concat = attributes;
                        } else {
                            KMeta[] elems = currentClass.metaElements();
                            for (int i = 0; i < elems.length; i++) {
                                if (elems[i] != null && elems[i].metaType().equals(MetaType.ATTRIBUTE) && ((KMetaAttribute) elems[i]).key()) {
                                    String lvalue = params.get(elems[i].metaName());
                                    if (lvalue != null) {
                                        concat += lvalue;
                                    }
                                }
                            }
                        }
                    }
                    long objectUUID = kObjectIndex.getIndex(concat);
                    if (objectUUID == KConfig.NULL_LONG) {
                        if (Checker.isDefined(callback)) {
                            callback.on(null);
                        }
                    } else {
                        _manager.lookup(universe, time, objectUUID, callback);
                    }
                }
            });
        }
    }

    private KStringMap<String> buildParams(String p_paramString) {
        KStringMap<String> params = new ArrayStringMap<String>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        int iParam = 0;
        int lastStart = iParam;
        while (iParam < p_paramString.length()) {
            if (p_paramString.charAt(iParam) == QueryEngine.VALS_SEP) {
                String p = p_paramString.substring(lastStart, iParam).trim();
                if (!PrimitiveHelper.equals(p, "")) {
                    String[] pArray = p.split(QueryEngine.VAL_SEP);
                    if (pArray.length > 1) {
                        params.put(pArray[0].trim(), pArray[1].trim());
                    }
                }
                lastStart = iParam + 1;
            }
            iParam = iParam + 1;
        }
        String lastParam = p_paramString.substring(lastStart, iParam).trim();
        if (!PrimitiveHelper.equals(lastParam, "")) {
            String[] pArray = lastParam.split(QueryEngine.VAL_SEP);
            if (pArray.length > 1) {
                params.put(pArray[0].trim(), pArray[1].trim());
            }
        }
        return params;
    }

    @Override
    public void indexByName(long universe, long time, String indexName, KCallback<KObjectIndex> callback) {
        _manager.index(universe, time, indexName, true, callback);
    }

    @Override
    public void findAll(KMetaClass metaClass, long universe, long time, KCallback<KObject[]> callback) {
        findAllByName(metaClass.metaName(), universe, time, callback);
    }

    @Override
    public void findAllByName(String indexName, long universe, long time, KCallback<KObject[]> callback) {
        _manager.index(universe, time, indexName, false, new KCallback<KObjectIndex>() {
            @Override
            public void on(KObjectIndex index) {
                if (index == null) {
                    if (callback != null) {
                        callback.on(new KObject[0]);
                    }
                } else {
                    _manager.lookupAllObjects(universe, time, index.values(), callback);
                }
            }
        });
    }

}

