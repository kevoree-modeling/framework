package org.kevoree.modeling.drivers.websocket.test;

import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.message.KMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class KContentDeliveryDriverMock implements KContentDeliveryDriver {

    public HashMap<String, String> alreadyPut = new HashMap<String, String>();

    @Override
    public void atomicGetIncrement(KContentKey key, KCallback<Short> callback) {
        callback.on(Short.parseShort("0"));
    }

    @Override
    public void put(KContentKey[] keys, String[] values, KCallback<Throwable> error, int excludeListener) {
        for (int i = 0; i < keys.length; i++) {
            alreadyPut.put(keys[i].toString(), values[i]);
        }
        msgCounter.countDown();
        error.on(null);
    }

    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = keys[i].toString();
        }
        callback.on(values);
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> error) {

    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        callback.on(null);
    }

    @Override
    public void close(KCallback<Throwable> callback) {

    }

    public ArrayList<KMessage> recMessages = new ArrayList<KMessage>();

    public CountDownLatch msgCounter = new CountDownLatch(1);

    @Override
    public int addUpdateListener(KContentUpdateListener interceptor) {
        return 0;
    }

    @Override
    public void removeUpdateListener(int id) {
    }


}
