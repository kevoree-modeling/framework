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
    public void atomicGetIncrement(long[] key, KCallback<Short> callback) {
        callback.on(Short.parseShort("0"));
    }

    @Override
    public void put(long[] keys, String[] values, KCallback<Throwable> error, int excludeListener) {
        int nbKeys = keys.length /3;
        for (int i = 0; i < nbKeys; i++) {
            alreadyPut.put(KContentKey.toString(keys, i), values[i]);
        }
        msgCounter.countDown();
        error.on(null);
    }

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        int nbKeys = keys.length /3;
        String[] values = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            values[i] = KContentKey.toString(keys,i);
        }
        callback.on(values);
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> error) {

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

    @Override
    public String[] peers() {
        return new String[0];
    }

    @Override
    public void sendToPeer(String peer, KMessage message) {

    }


}
