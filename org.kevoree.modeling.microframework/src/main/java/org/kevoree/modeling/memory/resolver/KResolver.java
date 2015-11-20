package org.kevoree.modeling.memory.resolver;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KPreparedLookup;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.scheduler.KTask;

import java.util.concurrent.atomic.AtomicReference;

public interface KResolver {

    KTask lookup(long universe, long time, long uuid, KCallback<KObject> callback);

    KTask lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback);

    KTask lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback);

    KTask lookupPreciseKeys(long[] keys, KCallback<KObject[]> callback);

    KTask lookupPrepared(KPreparedLookup preparedLookup, KCallback<KObject[]> callback);

    KObjectChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, AtomicReference<long[]> previousResolution);

    KObjectChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, AtomicReference<long[]> previousResolution);

    void indexObject(KObject obj);

    void resolveTimes(final long currentUniverse, final long currentUuid, final long startTime, final long endTime, KCallback<long[]> callback);
    
    int getRelatedKeysResultSize();

    void getRelatedKeys(long universe, long time, long uuid, long[] result);

}
