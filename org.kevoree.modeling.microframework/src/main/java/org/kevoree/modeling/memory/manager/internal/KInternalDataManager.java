package org.kevoree.modeling.memory.manager.internal;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.operation.KOperationManager;

import java.util.concurrent.atomic.AtomicReference;

public interface KInternalDataManager extends KDataManager {

    KListener createListener(long universe);

    KContentDeliveryDriver cdn();

    KObjectChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, AtomicReference<long[]> previousResolution);

    KObjectChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, AtomicReference<long[]> previousResolution);

    void initKObject(KObject obj);

    void initUniverse(long universe, long parent);

    long nextUniverseKey();

    long nextObjectKey();

    long nextModelKey();

    void delete(KUniverse universe, KCallback<Throwable> callback);

    KOperationManager operationManager();

    void setModel(KModel model);

    void resolveTimes(final long currentUniverse, final long currentUuid, final long startTime, final long endTime, KCallback<long[]> callback);

    int spaceSize();

    void printDebug();

}
