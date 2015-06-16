package org.kevoree.modeling.cdn;

import org.kevoree.modeling.*;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.message.KMessage;

public interface KContentDeliveryDriver {

    void get(KContentKey[] keys, KCallback<String[]> callback);

    void atomicGetIncrement(KContentKey key, KCallback<Short> cb);

    void put(KContentPutRequest request, KCallback<Throwable> error);

    void remove(String[] keys, KCallback<Throwable> error);

    void connect(KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

    void registerListener(long groupId, KObject origin, KEventListener listener);

    void registerMultiListener(long groupId, KUniverse origin, long[] objects, KEventMultiListener listener);

    void unregisterGroup(long groupId);

    void send(KMessage msgs);

    void setManager(KMemoryManager manager);

}