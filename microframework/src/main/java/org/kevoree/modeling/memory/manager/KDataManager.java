package org.kevoree.modeling.memory.manager;

import org.kevoree.modeling.*;
import org.kevoree.modeling.util.maths.structure.blas.KBlas;

public interface KDataManager {

    void index(long universe, long time, String indexName, boolean createIfAbsent, KCallback<KObjectIndex> callback);

    void lookup(long universe, long time, long uuid, KCallback<KObject> callback);

    void lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback);

    KObject[] syncLookupAllObjects(long universe, long time, long[] uuids);

    void lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback);

    KPreparedLookup createPreparedLookup(int size);

    void lookupPrepared(KPreparedLookup prepared, KCallback<KObject[]> callback);

    void save(KCallback<Throwable> callback);

    KModel model();

    void connect(KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

    KBlas blas();

}
