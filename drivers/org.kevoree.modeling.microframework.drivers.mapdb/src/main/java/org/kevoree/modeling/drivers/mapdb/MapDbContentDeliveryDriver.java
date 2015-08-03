package org.kevoree.modeling.drivers.mapdb;

import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
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
    public void atomicGetIncrement(KContentKey key, KCallback<Short> cb) {
        String result = (String) m.get(key.toString());
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
        m.put(key.toString(), nextV + "");
        cb.on(previousV);
    }

    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        String[] results = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            results[i] = (String) m.get(keys[i].toString());
        }
        callback.on(results);
    }

    @Override
    public synchronized void put(KContentKey[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        if (p_keys.length != p_values.length) {
            p_callback.on(new Exception("Bad Put Usage !"));
        } else {
            for (int i = 0; i < p_keys.length; i++) {
                m.put(p_keys[i].toString(), p_values[i]);
            }
            if (additionalInterceptors != null) {
                additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                    @Override
                    public void on(int key, KContentUpdateListener value) {
                        if (value != null && key != excludeListener) {
                            value.on(p_keys);
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
    public void remove(String[] keys, KCallback<Throwable> error) {

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

}
