package org.kevoree.modeling.drivers.mongodb;

import com.mongodb.*;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.KMessage;

import java.net.UnknownHostException;
import java.util.Random;

public class MongoDbContentDeliveryDriver implements KContentDeliveryDriver {

    private DB db = null;
    private MongoClient mongoClient = null;
    private DBCollection table = null;

    private String host = null;
    private Integer port = null;
    private String dbName = null;

    public MongoDbContentDeliveryDriver(String host, Integer port, String dbName) throws UnknownHostException {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
    }

    private static final String KMF_COL = "kmfall";

    private static final String KMF_KEY = "@key";

    private static final String KMF_VAL = "@val";

    @Override
    public void atomicGetIncrement(long[] p_key, KCallback<Short> cb) {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(KMF_KEY, KContentKey.toString(p_key, 0));
        DBCursor cursor = table.find(searchQuery);
        String result = "0";
        if (cursor.count() > 1) {
            DBObject objectResult = cursor.next();
            result = objectResult.get(KMF_VAL).toString();
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
        BasicDBObject newValue = new BasicDBObject();
        newValue.append(KMF_KEY, KContentKey.toString(p_key, 0));
        newValue.append(KMF_VAL, nextV);
        table.update(searchQuery, newValue, true, false);
        cb.on(previousV);
    }


    @Override
    public void get(long[] p_keys, KCallback<String[]> callback) {
        int nbKeys = p_keys.length / 3;
        String[] result = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put(KMF_KEY, KContentKey.toString(p_keys, i));
            DBCursor cursor = table.find(searchQuery);
            if (cursor.count() == 1) {
                DBObject objectResult = cursor.next();
                result[i] = objectResult.get(KMF_VAL).toString();
            }
        }
        if (callback != null) {
            callback.on(result);
        }
    }

    @Override
    public synchronized void put(long[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        int nbKeys = p_keys.length / 3;
        for (int i = 0; i < nbKeys; i++) {
            BasicDBObject originalObjectQuery = new BasicDBObject();
            originalObjectQuery.put(KMF_KEY, KContentKey.toString(p_keys, i));
            BasicDBObject newValue = new BasicDBObject();
            newValue.append(KMF_KEY, KContentKey.toString(p_keys, i));
            newValue.append(KMF_VAL, p_values[i]);
            table.update(originalObjectQuery, newValue, true, false);
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
    public void remove(long[] keys, KCallback<Throwable> error) {
        if (error != null) {
            error.on(null);
        }
    }

    @Override
    public void close(KCallback<Throwable> error) {
        if (mongoClient != null) {
            mongoClient.close();
        }
        mongoClient = null;
        db = null;
        table = null;
        if (error != null) {
            error.on(null);
        }
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB(dbName);
        table = db.getCollection(KMF_COL);
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
