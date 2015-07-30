package org.kevoree.modeling.memory.manager.internal;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.operation.KOperationManager;

public interface KInternalDataManager extends KDataManager {

    void save(KObject src, KCallback<Throwable> callback);

    void discard(KUniverse universe, KCallback<Throwable> callback);

    void delete(KUniverse universe, KCallback<Throwable> callback);

    KListener createListener(long universe);

    KContentDeliveryDriver cdn();

    KMemoryStorage cache();

    KMemoryChunk segment(long universe, long time, long uuid, boolean resolvePreviousSegment, KMetaClass metaClass, KMemorySegmentResolutionTrace resolutionTrace);

    void initKObject(KObject obj);

    void initUniverse(KUniverse universe, KUniverse parent);

    long nextUniverseKey();

    long nextObjectKey();

    long nextModelKey();

    KOperationManager operationManager();

    long parentUniverseKey(long currentUniverseKey);

    long[] descendantsUniverseKeys(long currentUniverseKey);

    void reload(KContentKey[] keys, KCallback<Throwable> callback);

    void cleanCache();

    void isUsed(KObject origin, boolean state);

    void setModel(KModel model);
    
}
