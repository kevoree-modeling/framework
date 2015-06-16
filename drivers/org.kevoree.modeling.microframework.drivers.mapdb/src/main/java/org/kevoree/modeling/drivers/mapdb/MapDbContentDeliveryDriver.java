package org.kevoree.modeling.drivers.mapdb;

import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentPutRequest;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.event.impl.LocalEventListeners;
import org.kevoree.modeling.message.KMessage;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Map;

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
    public void put(KContentPutRequest request, KCallback<Throwable> error) {
        for (int i = 0; i < request.size(); i++) {
            m.put(request.getKey(i).toString(), request.getContent(i).toString());
        }
        error.on(null);
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

    private LocalEventListeners localEventListeners = new LocalEventListeners();

    @Override
    public void registerListener(long p_groupId, KObject p_origin, KEventListener p_listener) {
        localEventListeners.registerListener(p_groupId, p_origin, p_listener);
    }

    @Override
    public void registerMultiListener(long p_groupId, KUniverse p_origin, long[] p_objects, KEventMultiListener p_listener) {
        localEventListeners.registerListenerAll(p_groupId, p_origin.key(), p_objects, p_listener);
    }

    @Override
    public void unregisterGroup(long p_groupId) {
        localEventListeners.unregister(p_groupId);
    }

    @Override
    public void send(KMessage msgs) {
        //No Remote send since LevelDB do not provide message brokering
        localEventListeners.dispatch(msgs);
    }

    @Override
    public void setManager(KMemoryManager manager) {
        localEventListeners.setManager(manager);
    }

}
