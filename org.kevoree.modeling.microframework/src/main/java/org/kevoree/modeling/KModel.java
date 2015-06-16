package org.kevoree.modeling;

import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.operation.KOperation;
import org.kevoree.modeling.scheduler.KScheduler;

public interface KModel<A extends KUniverse> {

    long key();

    A newUniverse();

    A universe(long key);

    KMemoryManager manager();

    KModel<A> setContentDeliveryDriver(KContentDeliveryDriver dataBase);

    KModel<A> setScheduler(KScheduler scheduler);

    void setOperation(KMetaOperation metaOperation, KOperation operation);

    void setInstanceOperation(KMetaOperation metaOperation, KObject target, KOperation operation);

    KMetaModel metaModel();

    KDefer defer();

    void save(KCallback cb);

    void discard(KCallback cb);

    void connect(KCallback cb);

    void close(KCallback cb);

    void clearListenerGroup(long groupID);

    long nextGroup();

    KObject createByName(String metaClassName, long universe, long time);

    KObject create(KMetaClass clazz, long universe, long time);

}