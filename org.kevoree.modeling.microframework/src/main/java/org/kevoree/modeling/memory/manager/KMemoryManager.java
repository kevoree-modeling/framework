package org.kevoree.modeling.memory.manager;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.struct.cache.KCache;
import org.kevoree.modeling.memory.KMemoryFactory;
import org.kevoree.modeling.memory.struct.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.operation.KOperationManager;

public interface KMemoryManager {

    KContentDeliveryDriver cdn();

    KModel model();

    KCache cache();

    void lookup(long universe, long time, long uuid, KCallback<KObject> callback);

    void lookupAllObjects(long universe, long time, long[] uuid, KCallback<KObject[]> callback);

    void lookupAllTimes(long universe, long[] time, long uuid, KCallback<KObject[]> callback);

    void lookupAllObjectsTimes(long universe, long[] time, long[] uuid, KCallback<KObject[]> callback);

    KMemoryChunk segment(long universe, long time, long uuid, boolean resolvePreviousSegment, KMetaClass metaClass, KMemorySegmentResolutionTrace resolutionTrace);

    void save(KObject src, KCallback<Throwable> callback);

    void discard(KUniverse universe, KCallback<Throwable> callback);

    void delete(KUniverse universe, KCallback<Throwable> callback);

    void initKObject(KObject obj);

    void initUniverse(KUniverse universe, KUniverse parent);

    long nextUniverseKey();

    long nextObjectKey();

    long nextModelKey();

    void getRoot(long universe, long time, KCallback<KObject> callback);

    void setRoot(KObject newRoot, KCallback<Throwable> callback);

    void setContentDeliveryDriver(KContentDeliveryDriver driver);

    void setScheduler(KScheduler scheduler);

    KOperationManager operationManager();

    void connect(KCallback<Throwable> callback);

    void close(KCallback<Throwable> callback);

    long parentUniverseKey(long currentUniverseKey);

    long[] descendantsUniverseKeys(long currentUniverseKey);

    void reload(KContentKey[] keys, KCallback<Throwable> callback);

    void cleanCache();

    void setFactory(KMemoryFactory factory);

    KListener createListener(long universe);

}
