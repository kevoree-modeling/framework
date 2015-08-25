package org.kevoree.modeling.cdn;

import org.kevoree.modeling.*;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.message.KMessage;

public interface KContentDeliveryDriver {

    void get(long[] keys, KCallback<String[]> callback);

    void atomicGetIncrement(long[] key, KCallback<Short> cb);

    void put(long[] keys, String[] values, KCallback<Throwable> error, int excludeListener);

    void remove(long[] keys, KCallback<Throwable> error);

    void connect(KModel model, KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

    int addUpdateListener(KContentUpdateListener interceptor);

    void removeUpdateListener(int id);

    String[] peers();

    void sendToPeer(String peer, KMessage message, KCallback<KMessage> callback);
}