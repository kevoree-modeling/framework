package org.kevoree.modeling.databases.chronicle;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.ThrowableCallback;
import org.kevoree.modeling.api.memory.cdn.KContentDeliveryDriver;

import java.io.File;
import java.io.IOException;

/**
 * Created by duke on 11/4/14.
 */
public class ChronicleContentDeliveryDriver implements KContentDeliveryDriver {

    private ChronicleMap<String, String> raw;

    public ChronicleContentDeliveryDriver(String storagePath) throws IOException {
        if (storagePath == null) {
            raw = ChronicleMapBuilder.of(String.class, String.class).create();
        } else {
            File location = new File(storagePath);
            if (!location.exists()) {
                location.mkdirs();
            }
            File targetDB = new File(location, "data");
            raw = ChronicleMapBuilder.of(String.class, String.class).createPersistedTo(targetDB);
        }
    }

    @Override
    public void get(String[] keys, ThrowableCallback<String[]> callback) {
        String[] result = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            result[i] = raw.get(keys[i]);
        }
        if (callback != null) {
            callback.on(result, null);
        }
    }

    @Override
    public void put(String[][] payloads, Callback<Throwable> error) {
        for (int i = 0; i < payloads.length; i++) {
            raw.put(payloads[i][0], payloads[i][1]);
        }
        if (error != null) {
            error.on(null);
        }
    }

    @Override
    public void remove(String[] keys, Callback<Throwable> error) {
        try {
            for (int i = 0; i < keys.length; i++) {
                raw.remove(keys[i]);
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
    public void commit(Callback<Throwable> error) {
        try {
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
    public void connect(Callback<Throwable> callback) {
        //noop
        if (callback != null) {
            callback.on(null);
        }
    }

    @Override
    public void close(Callback<Throwable> error) {
        try {
            raw.close();
            if (error != null) {
                error.on(null);
            }
        } catch (Exception e) {
            if (error != null) {
                error.on(e);
            }
        }
    }
}
