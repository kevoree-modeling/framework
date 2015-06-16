package org.kevoree.modeling.databases.redis;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentPutRequest;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.message.impl.Events;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;
import org.kevoree.modeling.event.impl.LocalEventListeners;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.List;

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
                            _localEventListeners.dispatch(msg);
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

    private LocalEventListeners _localEventListeners = new LocalEventListeners();

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
    public void put(KContentPutRequest request, KCallback<Throwable> error) {
        String[] elems = new String[request.size() * 2];
        for (int i = 0; i < request.size(); i++) {
            elems[(i * 2)] = request.getKey(i).toString();
            elems[(i * 2) + 1] = request.getContent(i);
        }
        if (jedis != null) {
            jedis.mset(elems);
        }
        if (error != null) {
            error.on(null);
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
    public void registerListener(long p_groupId, KObject p_origin, KEventListener p_listener) {
        _localEventListeners.registerListener(p_groupId, p_origin, p_listener);
    }

    @Override
    public void registerMultiListener(long p_groupId, KUniverse p_origin, long[] p_objects, KEventMultiListener p_listener) {
        _localEventListeners.registerListenerAll(p_groupId, p_origin.key(), p_objects, p_listener);
    }

    @Override
    public void unregisterGroup(long groupId) {
        throw new RuntimeException("Not Implemented Yet!");
    }

    @Override
    public void send(KMessage msg) {
        _localEventListeners.dispatch(msg);
        if (msg instanceof Events) {
            jedis.publish("kmf", msg.json());
        }
    }

    private KMemoryManager _manager;

    @Override
    public void setManager(KMemoryManager p_manager) {
        _manager = p_manager;
        _localEventListeners.setManager(p_manager);
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        //noop
        if (callback != null) {
            callback.on(null);
        }
    }

}
