package org.kevoree.modeling;

import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.operation.KOperation;

public interface KModel<A extends KUniverse> {

    long key();

    A newUniverse();

    A universe(long key);

    KDataManager manager();

    void setOperation(KMetaOperation metaOperation, KOperation operation);

    void setInstanceOperation(KMetaOperation metaOperation, KObject target, KOperation operation);

    KMetaModel metaModel();

    KDefer defer();

    void save(KCallback cb);

    void connect(KCallback cb);

    void close(KCallback cb);

    KObject createByName(String metaClassName, long universe, long time);

    KObject create(KMetaClass clazz, long universe, long time);

    void lookup(long universe, long time, long uuid, KCallback<KObject> cb);

    KListener createListener(long universe);

}