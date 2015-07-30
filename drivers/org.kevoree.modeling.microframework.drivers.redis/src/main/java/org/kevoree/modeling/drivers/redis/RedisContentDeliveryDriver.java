package org.kevoree.modeling.drivers.redis;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.map.KIntMapCallBack;
import org.kevoree.modeling.memory.map.impl.ArrayIntMap;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;
import org.kevoree.modeling.message.impl.Events;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Random;

public class RedisContentDeliveryDriver implements KContentDeliveryDriver {

    private Jedis jedis = null;

    private Thread listenerThread = null;

    private Jedis jedis2;

    public RedisContentDeliveryDriver(String ip, Integer port) {
        jedis = new Jedis(ip, port);
        listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jedis2 = new Jedis(ip, port);
                    jedis2.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            KMessage msg = KMessageLoader.load(message);
                            if (msg instanceof Events) {
                                additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                                    @Override
                                    public void on(int key, KContentUpdateListener value) {
                                        value.on(((Events) msg).allKeys());
                                    }
                                });
                            }
                        }
                    }, "kmf");
                } catch (Exception e) {
                    System.err.println("Error during Event Listener Redis");
                    e.printStackTrace();
                }
            }
        });
        listenerThread.start();
    }

    @Override
    public void atomicGetIncrement(KContentKey key, KCallback<Short> cb) {
        String result = jedis.get(key.toString());
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
        //TODO use the nativeInc method
        jedis.set(key.toString(), "" + nextV);
        cb.on(previousV);
    }


    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        String[] flatKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            flatKeys[i] = keys[i].toString();
        }
        List<String> values = jedis.mget(flatKeys);
        if (callback != null) {
            callback.on(values.toArray(new String[values.size()]));
        }
    }

    @Override
    public synchronized void put(KContentKey[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        String[] elems = new String[p_keys.length * 2];
        for (int i = 0; i < p_keys.length; i++) {
            elems[(i * 2)] = p_keys[i].toString();
            elems[(i * 2) + 1] = p_values[i];
        }
        if (jedis != null) {
            jedis.mset(elems);
        }
        Events events = new Events(p_keys);
        jedis.publish("kmf", events.json());
        if (p_callback != null) {
            p_callback.on(null);
        }
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> error) {
        jedis.del(keys);
    }

    @Override
    public void close(KCallback<Throwable> error) {
        if (jedis != null) {
            jedis.close();
        }
        if (jedis2 != null) {
            jedis2.close();
        }
        listenerThread.stop();
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
