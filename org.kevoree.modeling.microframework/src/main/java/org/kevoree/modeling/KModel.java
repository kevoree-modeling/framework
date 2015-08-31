package org.kevoree.modeling;

import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.traversal.KTraversal;

public interface KModel<A extends KUniverse> {

    /** Current Model state variables accessor */
    long key();

    A newUniverse();

    A universe(long key);

    KDataManager manager();

    KMetaModel metaModel();

    /** Create a new KDefer */
    KDefer defer();

    /** Operation Management */
    void setOperation(KMetaOperation metaOperation, KOperation operation);

    void setOperationByName(String metaClassName, String metaOperationName, KOperation operation);

    /** Life cycle management */
    void save(KCallback callback);

    void connect(KCallback callback);

    void disconnect(KCallback callback);

    /** Lookup primitives */
    void lookup(long universe, long time, long uuid, KCallback<KObject> cb);

    void lookupAll(long universe, long time, long[] uuids, KCallback<KObject[]> cb);

    /** Creation methods */
    KObject createByName(String metaClassName, long universe, long time);

    KObject create(KMetaClass clazz, long universe, long time);

    KListener createListener(long universe);

    KModelContext createModelContext();

    KTraversal createTraversal(KObject[] startingElements);

    KTraversal createReusableTraversal();
    
}