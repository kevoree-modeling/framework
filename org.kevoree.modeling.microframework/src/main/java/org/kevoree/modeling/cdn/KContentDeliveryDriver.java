package org.kevoree.modeling.cdn;

import org.kevoree.modeling.*;

public interface KContentDeliveryDriver {

    void get(KContentKey[] keys, KCallback<String[]> callback);

    void atomicGetIncrement(KContentKey key, KCallback<Short> cb);

    void put(KContentKey[] keys, String[] values, KCallback<Throwable> error, int excludeListener);

    void remove(String[] keys, KCallback<Throwable> error);

    void connect(KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

    int addUpdateListener(KContentUpdateListener interceptor);

    void removeUpdateListener(int id);

}