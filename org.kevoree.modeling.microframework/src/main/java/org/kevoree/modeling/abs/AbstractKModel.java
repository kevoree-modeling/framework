package org.kevoree.modeling.abs;

import org.kevoree.modeling.*;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.KOperation;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.defer.impl.Defer;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.impl.Traversal;
import org.kevoree.modeling.util.Checker;

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
    public void close(KCallback cb) {
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
    public void save(KCallback cb) {
        _manager.save(cb);
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
    public void lookup(long p_universe, long p_time, long p_uuid, KCallback<KObject> cb) {
        _manager.lookup(p_universe, p_time, p_uuid, cb);
    }

    @Override
    public void lookupAll(long p_universe, long p_time, long[] p_uuids, KCallback<KObject[]> cb) {
        _manager.lookupAllObjects(p_universe, p_time, p_uuids, cb);
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

}

