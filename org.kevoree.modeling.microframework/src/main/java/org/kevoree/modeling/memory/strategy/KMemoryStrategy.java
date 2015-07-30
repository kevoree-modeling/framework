package org.kevoree.modeling.memory.strategy;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;

public interface KMemoryStrategy {

    KMemoryChunk newCacheSegment();

    KLongTree newLongTree();

    KLongLongTree newLongLongTree();

    KUniverseOrderMap newUniverseMap(int initSize, String className);

    KMemoryElement newFromKey(long universe, long time, long uuid);

    KMemoryStorage newStorage(KMemoryStrategy strategy);

    KCache newCache(KMemoryStorage storage);

}
