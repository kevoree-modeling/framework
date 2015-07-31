package org.kevoree.modeling.memory.storage.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.HeapMemoryChunk;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.ArrayUniverseOrderMap;
import org.kevoree.modeling.memory.storage.BaseKMemoryStorageTest;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.memory.tree.impl.ArrayLongLongTree;
import org.kevoree.modeling.memory.tree.impl.ArrayLongTree;

public class ArrayMemoryStorageTest extends BaseKMemoryStorageTest {

    @Override
    public KMemoryStorage createKMemoryStorage() {
        return new HeapMemoryStorage(null);
    }

    @Override
    public KLongLongTree createKLongLongTree() {
        return new ArrayLongLongTree();
    }

    @Override
    public KLongTree createKLongTree() {
        return new ArrayLongTree();
    }

    @Override
    public KUniverseOrderMap createKUniverseOrderMap() {
        return new ArrayUniverseOrderMap(10, KConfig.CACHE_LOAD_FACTOR, null);
    }

    @Override
    public KMemoryChunk createKMemoryChunk() {
        return new HeapMemoryChunk();
    }
}
