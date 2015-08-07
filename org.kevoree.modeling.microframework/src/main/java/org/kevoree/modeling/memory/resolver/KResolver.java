package org.kevoree.modeling.memory.resolver;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaClass;

public interface KResolver {

    Runnable lookup(long universe, long time, long uuid, KCallback<KObject> callback);

    Runnable lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback);

    Runnable lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback);

    KObjectChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution);

    KObjectChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution);

    void indexObject(KObject obj);

    short typeFromKey(long universe, long time, long uuid);

    void resolveTimes(final long currentUniverse, final long currentUuid, final long startTime, final long endTime, KCallback<long[]> callback);

    void getRoot(long universe, long time, KCallback<KObject> callback);

    void setRoot(KObject newRoot, KCallback<Throwable> callback);

    long[] getRelatedKeys(long uuid, long[] previousResolution);

}
