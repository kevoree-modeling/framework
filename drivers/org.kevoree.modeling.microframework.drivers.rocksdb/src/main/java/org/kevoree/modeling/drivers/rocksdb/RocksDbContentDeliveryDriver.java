package org.kevoree.modeling.drivers.rocksdb;

import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.map.KIntMapCallBack;
import org.kevoree.modeling.memory.map.impl.ArrayIntMap;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RocksDbContentDeliveryDriver implements KContentDeliveryDriver {

    private Options options;

    private RocksDB db;

    public RocksDbContentDeliveryDriver(String storagePath) throws IOException, RocksDBException {
        options = new Options();
        options.setCreateIfMissing(true);
        File location = new File(storagePath);
        if (!location.exists()) {
            location.mkdirs();
        }
        File targetDB = new File(location, "data");
        targetDB.mkdirs();
        db = RocksDB.open(options, targetDB.getAbsolutePath());
    }

    @Override
    public synchronized void put(KContentKey[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        WriteBatch batch = new WriteBatch();
        for (int i = 0; i < p_keys.length; i++) {
            batch.put(p_keys[i].toString().getBytes(), p_values[i].getBytes());
        }
        WriteOptions options = new WriteOptions();
        options.setSync(true);
        try {
            db.write(options, batch);
        } catch (RocksDBException e) {
            e.printStackTrace();
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


    @Override
    public void atomicGetIncrement(KContentKey key, KCallback<Short> cb) {
        try {
            String result = new String(db.get(key.toString().getBytes()));
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
            WriteBatch batch = new WriteBatch();
            batch.put(key.toString().getBytes(), (nextV + "").getBytes());

            db.write(new WriteOptions().setSync(true), batch);
            cb.on(previousV);
        } catch (RocksDBException e) {
            e.printStackTrace();
            cb.on(null);
        }
    }

    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        String[] result = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            try {
                result[i] = new String(db.get(keys[i].toString().getBytes()));

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (callback != null) {
                callback.on(result);
            }
        }
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> error) {
        try {
            for (int i = 0; i < keys.length; i++) {
                db.remove(keys[i].getBytes());
            }
            if (error != null) {
                error.on(null);
            }
        } catch (Exception e) {
            if (error != null) {
                error.on(e);
            }
        }
    }

    @Override
    public void close(KCallback<Throwable> error) {
        try {
            WriteOptions options = new WriteOptions();
            options.sync();
            db.write(options, new WriteBatch());
            db.close();
            if (error != null) {
                error.on(null);
            }
        } catch (Exception e) {
            if (error != null) {
                error.on(e);
            }
        }
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        //noop
        if (callback != null) {
            callback.on(null);
        }
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
