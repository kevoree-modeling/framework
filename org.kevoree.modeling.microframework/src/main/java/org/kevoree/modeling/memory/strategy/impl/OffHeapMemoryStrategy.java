package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.cache.impl.SingleChainWeakRefCache;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.storage.impl.OffHeapMemoryMemoryStorage;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.ArrayUniverseOrderMap;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.OffHeapMemoryChunk;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongTree;

/**
 * @ignore ts
 */
public class OffHeapMemoryStrategy implements KMemoryStrategy {

    @Override
    public KMemoryChunk newCacheSegment() {
        return new OffHeapMemoryChunk();
    }

    @Override
    public KLongTree newLongTree() {
        return new OffHeapLongTree();
    }

    @Override
    public KLongLongTree newLongLongTree() {
        return new OffHeapLongLongTree();
    }

    @Override
    public KUniverseOrderMap newUniverseMap(int initSize, String p_className) {
        return new OffHeapUniverseOrderMap(initSize, KConfig.CACHE_LOAD_FACTOR, p_className);
    }

    @Override
    public KMemoryElement newFromKey(long universe, long time, long uuid) {
        KMemoryElement result;
        boolean isUniverseNotNull = universe != KConfig.NULL_LONG;
        if (KConfig.END_OF_TIME == uuid) {
            if (isUniverseNotNull) {
                result = newLongLongTree();
            } else {
                result = newUniverseMap(0, null);
            }
        } else {
            boolean isTimeNotNull = time != KConfig.NULL_LONG;
            boolean isObjNotNull = uuid != KConfig.NULL_LONG;
            if (isUniverseNotNull && isTimeNotNull && isObjNotNull) {
                result = newCacheSegment();
            } else if (isUniverseNotNull && !isTimeNotNull && isObjNotNull) {
                result = newLongTree();
            } else {
                result = newUniverseMap(0, null);
            }
        }
        return result;
    }

    @Override
    public KMemoryStorage newStorage() {
        return new OffHeapMemoryMemoryStorage();
    }

    @Override
    public KCache newCache(KMemoryStorage p_storage) {
        return new SingleChainWeakRefCache(p_storage);
    }

}
