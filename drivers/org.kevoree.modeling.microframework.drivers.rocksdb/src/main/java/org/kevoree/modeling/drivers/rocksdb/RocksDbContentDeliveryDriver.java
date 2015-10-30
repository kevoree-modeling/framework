package org.kevoree.modeling.drivers.rocksdb;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.KMessage;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RocksDbContentDeliveryDriver implements KContentDeliveryDriver {

    private Options options;

    private RocksDB _db;

    private static final String _connectedError = "PLEASE CONNECT YOUR DATABASE FIRST";

    private boolean _isConnected = false;

    private final String _storagePath;

    public RocksDbContentDeliveryDriver(String storagePath) throws IOException, RocksDBException {
        this._storagePath = storagePath;
    }

    @Override
    public void put(long[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = p_keys.length / 3;
        WriteBatch batch = new WriteBatch();
        for (int i = 0; i < nbKeys; i++) {
            batch.put(KContentKey.toString(p_keys, i).getBytes(), p_values[i].getBytes());
        }
        WriteOptions options = new WriteOptions();
        options.setSync(true);
        try {
            _db.write(options, batch);
        } catch (RocksDBException e) {
            e.printStackTrace();
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


    @Override
    public void atomicGetIncrement(long[] key, KCallback<Short> cb) {
        try {
            byte[] bulkResult = _db.get(KContentKey.toString(key, 0).getBytes());
            String result = null;
            if (bulkResult != null) {
                result = new String(bulkResult);
            }
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
            batch.put(KContentKey.toString(key, 0).getBytes(), (nextV + "").getBytes());
            _db.write(new WriteOptions().setSync(true), batch);
            cb.on(previousV);
        } catch (RocksDBException e) {
            e.printStackTrace();
            cb.on(null);
        }
    }

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = keys.length / 3;
        String[] result = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            try {
                byte[] res = _db.get(KContentKey.toString(keys, i).getBytes());
                String casted = null;
                if(res != null){
                    casted = new String(res);
                } else {
                    casted = new String(new byte[0]);
                }
                result[i] = casted;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (callback != null) {
            callback.on(result);
        }
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> error) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = keys.length / 3;
        try {
            for (int i = 0; i < nbKeys; i++) {
                _db.remove(KContentKey.toString(keys, i).getBytes());
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
            _db.write(options, new WriteBatch());
            _db.close();
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
        if (_isConnected) {
            if (callback != null) {
                callback.on(null);
            }
            return;
        }
        options = new Options();
        options.setCreateIfMissing(true);
        File location = new File(_storagePath);
        if (!location.exists()) {
            location.mkdirs();
        }
        File targetDB = new File(location, "data");
        targetDB.mkdirs();
        try {
            _db = RocksDB.open(options, targetDB.getAbsolutePath());
            _isConnected = true;
            if (callback != null) {
                callback.on(null);
            }
        } catch (RocksDBException e) {
            if (callback != null) {
                callback.on(e);
            }
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
