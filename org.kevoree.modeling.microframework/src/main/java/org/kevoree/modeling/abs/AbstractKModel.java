package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.operation.KOperation;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.KUniverse;
import org.kevoree.modeling.memory.manager.impl.HeapMemoryManager;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.defer.impl.Defer;
import org.kevoree.modeling.util.Checker;

public abstract class AbstractKModel<A extends KUniverse> implements KModel<A> {

    final protected KMemoryManager _manager;

    final private long _key;

    protected AbstractKModel() {
        _manager = new HeapMemoryManager(this);
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
    public KMemoryManager manager() {
        return _manager;
    }

    @Override
    public A newUniverse() {
        long nextKey = _manager.nextUniverseKey();
        final A newDimension = internalCreateUniverse(nextKey);
        manager().initUniverse(newDimension, null);
        return newDimension;
    }

    protected abstract A internalCreateUniverse(long universe);

    protected abstract KObject internalCreateObject(long universe, long time, long uuid, KMetaClass clazz);

    public KObject createProxy(long universe, long time, long uuid, KMetaClass clazz) {
        return internalCreateObject(universe, time, uuid, clazz);
    }

    @Override
    public A universe(long key) {
        A newDimension = internalCreateUniverse(key);
        manager().initUniverse(newDimension, null);
        return newDimension;
    }

    @Override
    public void save(KCallback cb) {
        _manager.save(cb);
    }

    @Override
    public void discard(KCallback cb) {
        _manager.discard(null, cb);
    }

    @Override
    public KModel<A> setContentDeliveryDriver(KContentDeliveryDriver p_driver) {
        manager().setContentDeliveryDriver(p_driver);
        return this;
    }

    @Override
    public KModel<A> setScheduler(KScheduler p_scheduler) {
        manager().setScheduler(p_scheduler);
        return this;
    }

    @Override
    public void setOperation(KMetaOperation metaOperation, KOperation operation) {
        manager().operationManager().registerOperation(metaOperation, operation, null);
    }

    @Override
    public void setInstanceOperation(KMetaOperation metaOperation, KObject target, KOperation operation) {
        manager().operationManager().registerOperation(metaOperation, operation, target);
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
    public void clearListenerGroup(long groupID) {
        manager().cdn().unregisterGroup(groupID);
    }

    @Override
    public long nextGroup() {
        return this.manager().nextGroupKey();
    }

    @Override
    public KObject create(KMetaClass clazz, long universe, long time) {
        if (!Checker.isDefined(clazz)) {
            return null;
        }
        KObject newObj = internalCreateObject(universe, time, _manager.nextObjectKey(), clazz);
        if (newObj != null) {
            _manager.initKObject(newObj);
        }
        return newObj;
    }

    @Override
    public KObject createByName(String metaClassName, long universe, long time) {
        return create(_manager.model().metaModel().metaClassByName(metaClassName), universe, time);
    }

}
