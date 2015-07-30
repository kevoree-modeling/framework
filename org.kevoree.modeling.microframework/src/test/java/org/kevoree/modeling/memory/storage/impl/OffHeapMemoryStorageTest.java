package org.kevoree.modeling.memory.storage.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.OffHeapMemoryChunk;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.OffHeapUniverseOrderMap;
import org.kevoree.modeling.memory.storage.BaseKMemoryStorageTest;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.strategy.impl.OffHeapMemoryStrategy;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongTree;

public class OffHeapMemoryStorageTest extends BaseKMemoryStorageTest {

    @Override
    public KMemoryStorage createKMemoryStorage() {
        return new OffHeapMemoryMemoryStorage(new OffHeapMemoryStrategy());
    }

    @Override
    public KLongLongTree createKLongLongTree() {
        return new OffHeapLongLongTree();
    }

    @Override
    public KLongTree createKLongTree() {
        return new OffHeapLongTree();
    }

    @Override
    public KUniverseOrderMap createKUniverseOrderMap() {
        return new OffHeapUniverseOrderMap(10, KConfig.CACHE_LOAD_FACTOR, null);
    }

    @Override
    public KMemoryChunk createKMemoryChunk() {
        return new OffHeapMemoryChunk();
    }

}
