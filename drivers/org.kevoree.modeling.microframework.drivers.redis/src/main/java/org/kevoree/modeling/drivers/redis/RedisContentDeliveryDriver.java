package org.kevoree.modeling.drivers.redis;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.impl.Message;
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
                            KMessage msg = Message.load(message);
                            if (msg != null && msg.type() != null && msg.type() == Message.EVENTS_TYPE) {
                                additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                                    @Override
                                    public void on(int key, KContentUpdateListener value) {
                                        value.onKeysUpdate(msg.keys());
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
    public void atomicGetIncrement(long[] key, KCallback<Short> cb) {
        String result = jedis.get(KContentKey.toString(key, 0));
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
    public void get(long[] keys, KCallback<String[]> callback) {
        int nbKeys = keys.length / 3;
        String[] flatKeys = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            flatKeys[i] = KContentKey.toString(keys, i);
        }
        List<String> values = jedis.mget(flatKeys);
        if (callback != null) {
            callback.on(values.toArray(new String[values.size()]));
        }
    }

    @Override
    public synchronized void put(long[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        int nbKeys = p_keys.length / 3;
        String[] elems = new String[nbKeys * 2];
        for (int i = 0; i < nbKeys; i++) {
            elems[(i * 2)] = KContentKey.toString(p_keys, i);
            elems[(i * 2) + 1] = p_values[i];
        }
        if (jedis != null) {
            jedis.mset(elems);
        }
        KMessage events = new Message();
        events.setType(Message.EVENTS_TYPE);
        events.setKeys(p_keys);
        jedis.publish("kmf", events.save());
        if (p_callback != null) {
            p_callback.on(null);
        }
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> error) {
        int nbKeys = keys.length / 3;
        String[] elems = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            elems[i] = KContentKey.toString(keys, i);
        }
        jedis.del(elems);
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
