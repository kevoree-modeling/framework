package org.kevoree.modeling.memory;

import org.kevoree.modeling.memory.struct.cache.KCache;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public interface KMemoryFactory {

    KMemoryChunk newCacheSegment();

    KLongTree newLongTree();

    KLongLongTree newLongLongTree();

    KUniverseOrderMap newUniverseMap(int initSize, String className);

    KMemoryElement newFromKey(long universe,long time, long uuid);

    KCache newCache();

}
