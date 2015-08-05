package org.kevoree.modeling.cdn;

import org.kevoree.modeling.*;

public interface KContentDeliveryDriver {

    void get(long[] keys, KCallback<String[]> callback);

    void atomicGetIncrement(long[] key, KCallback<Short> cb);

    void put(long[] keys, String[] values, KCallback<Throwable> error, int excludeListener);

    void remove(long[] keys, KCallback<Throwable> error);

    void connect(KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

    int addUpdateListener(KContentUpdateListener interceptor);

    void removeUpdateListener(int id);

}