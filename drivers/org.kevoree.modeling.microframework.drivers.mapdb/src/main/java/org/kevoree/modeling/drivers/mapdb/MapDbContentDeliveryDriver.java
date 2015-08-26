package org.kevoree.modeling.drivers.mapdb;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.KMessage;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Map;
import java.util.Random;

/**
 * Created by duke on 12/05/15.
 */
public class MapDbContentDeliveryDriver implements KContentDeliveryDriver {

    private File directory = null;
    private DB db;
    private Map m;

    public MapDbContentDeliveryDriver(File targetDir) {
        this.directory = targetDir;
    }

    @Override
    public void atomicGetIncrement(long[] key, KCallback<Short> cb) {
        String result = (String) m.get(KContentKey.toString(key, 0));
        short nextV;
        short previousV;
        if (result != null) {
            try {
                previousV = Short.parseShort(result);
            } catch (Exception e) {
                e.printStackTrace();
                previousV = Short.MIN_VALUE;
            }
        } else {
            previousV = 0;
        }
        if (previousV == Short.MAX_VALUE) {
            nextV = Short.MIN_VALUE;
        } else {
            nextV = (short) (previousV + 1);
        }
        m.put(KContentKey.toString(key, 0), nextV + "");
        cb.on(previousV);
    }

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        int nbKeys = keys.length / 3;
        String[] results = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            results[i] = (String) m.get(KContentKey.toString(keys, i));
        }
        callback.on(results);
    }

    @Override
    public synchronized void put(long[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        if (p_keys.length != p_values.length) {
            p_callback.on(new Exception("Bad Put Usage !"));
        } else {
            int nbKeys = p_keys.length / 3;
            for (int i = 0; i < nbKeys; i++) {
                m.put(KContentKey.toString(p_keys, i), p_values[i]);
            }
            if (additionalInterceptors != null) {
                additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                    @Override
                    public void on(int key, KContentUpdateListener value) {
                        if (value != null && key != excludeListener) {
                            value.onKeysUpdate(p_keys);
                        }
                    }
                });
            }
            if (p_callback != null) {
                p_callback.on(null);
            }
        }
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> error) {

    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        db = DBMaker.newMemoryDirectDB().transactionDisable().asyncWriteFlushDelay(100).newFileDB(directory).closeOnJvmShutdown().make();
        m = db.getTreeMap("test");
    }

    @Override
    public void close(KCallback<Throwable> callback) {
        db.close();
        db = null;
        m = null;
    }

    private ArrayIntMap<KContentUpdateListener> additionalInterceptors = null;

    /**
     * @ignore ts
     */
    private Random random = new Random();

    /**
     * @native ts
     * return Math.random();
     */
    private int randomInterceptorID() {
        return random.nextInt();
    }

    @Override
    public synchronized int addUpdateListener(KContentUpdateListener p_interceptor) {
        if (additionalInterceptors == null) {
            additionalInterceptors = new ArrayIntMap<KContentUpdateListener>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        }
        int newID = randomInterceptorID();
        additionalInterceptors.put(newID, p_interceptor);
        return newID;
    }

    @Override
    public synchronized void removeUpdateListener(int id) {
        if (additionalInterceptors != null) {
            additionalInterceptors.remove(id);
        }
    }

    @Override
    public String[] peers() {
        return new String[0];
    }

    @Override
    public void sendToPeer(String peer, KMessage message, KCallback<KMessage> callback) {
        if (callback != null) {
            callback.on(null);
        }
    }

}
