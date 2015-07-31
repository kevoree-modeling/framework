package org.kevoree.modeling.memory.manager;

import org.kevoree.modeling.*;

public interface KDataManager {

    void lookup(long universe, long time, long uuid, KCallback<KObject> callback);

    void lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback);

    void lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback);

    void lookupAllObjectsTimes(long universe, long[] times, long[] uuids, KCallback<KObject[]> callback);

    void saveAll(KCallback<Throwable> callback);

    void discard(KCallback<Throwable> callback);

    void clear();

    void getRoot(long universe, long time, KCallback<KObject> callback);

    void setRoot(KObject newRoot, KCallback<Throwable> callback);

    KModel model();

    void connect(KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

}
