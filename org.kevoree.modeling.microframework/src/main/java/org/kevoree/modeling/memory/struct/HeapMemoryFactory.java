package org.kevoree.modeling.memory.struct;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.struct.cache.KCache;
import org.kevoree.modeling.memory.struct.cache.impl.ArrayMemoryCache;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayUniverseOrderMap;
import org.kevoree.modeling.memory.struct.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.KMemoryFactory;
import org.kevoree.modeling.memory.struct.chunk.impl.HeapMemoryChunk;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.memory.struct.tree.impl.ArrayLongLongTree;
import org.kevoree.modeling.memory.struct.tree.impl.ArrayLongTree;

public class HeapMemoryFactory implements KMemoryFactory {

    @Override
    public KMemoryChunk newCacheSegment() {
        return new HeapMemoryChunk();
    }

    @Override
    public KLongTree newLongTree() {
        return new ArrayLongTree();
    }

    @Override
    public KLongLongTree newLongLongTree() {
        return new ArrayLongLongTree();
    }

    @Override
    public KUniverseOrderMap newUniverseMap(int initSize, String p_className) {
        return new ArrayUniverseOrderMap(initSize, KConfig.CACHE_LOAD_FACTOR, p_className);
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
    public KCache newCache() {
        return new ArrayMemoryCache();
    }

}
