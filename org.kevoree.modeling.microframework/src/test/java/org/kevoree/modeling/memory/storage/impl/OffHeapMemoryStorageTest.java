package org.kevoree.modeling.memory.storage.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.OffHeapMemoryChunk;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.OffHeapUniverseOrderMap;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.strategy.impl.OffHeapMemoryStrategy;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongTree;

public class OffHeapMemoryStorageTest {

    @Test
    public void test() {
        KMemoryStorage storage = new OffHeapMemoryMemoryStorage(new OffHeapMemoryStrategy());

        // KUniverseOrderMap
        KUniverseOrderMap map = new OffHeapUniverseOrderMap(10, KConfig.CACHE_LOAD_FACTOR, null);
        map.put(0, 0);
        map.put(1, 1);
        storage.putAndReplace(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, map);
        KUniverseOrderMap retrievedMap = (KUniverseOrderMap) storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        Assert.assertEquals(0, retrievedMap.get(0));
        Assert.assertEquals(1, retrievedMap.get(1));
        Assert.assertEquals(map.size(), retrievedMap.size());
        Assert.assertEquals(map.isDirty(), retrievedMap.isDirty());
        Assert.assertEquals(map.counter(), retrievedMap.counter());

        // KLongLongTree
        KLongLongTree longLongTree = new OffHeapLongLongTree();
        longLongTree.init(null, null);
        longLongTree.insert(0, 0);
        longLongTree.insert(1, 1);
        storage.putAndReplace(0, KConfig.NULL_LONG, KConfig.END_OF_TIME, longLongTree);
        KLongLongTree retrievedLongLongTree = (KLongLongTree) storage.get(0, KConfig.NULL_LONG, KConfig.END_OF_TIME);
        Assert.assertEquals(0, retrievedLongLongTree.lookupValue(0));
        Assert.assertEquals(1, retrievedLongLongTree.lookupValue(1));
        Assert.assertEquals(longLongTree.size(), retrievedLongLongTree.size());
        Assert.assertEquals(longLongTree.isDirty(), retrievedLongLongTree.isDirty());
        Assert.assertEquals(longLongTree.counter(), retrievedLongLongTree.counter());

        // KLongTree
        KLongTree longTree = new OffHeapLongTree();
        longTree.init(null, null);
        longTree.insert(0);
        longTree.insert(1);
        storage.putAndReplace(0, KConfig.NULL_LONG, 0, longTree);
        KLongTree retrievedLongTree = (KLongTree) storage.get(0, KConfig.NULL_LONG, 0);
        Assert.assertEquals(0, retrievedLongTree.lookup(0));
        Assert.assertEquals(1, retrievedLongTree.lookup(1));
        Assert.assertEquals(longTree.size(), retrievedLongTree.size());
        Assert.assertEquals(longTree.isDirty(), retrievedLongTree.isDirty());
        Assert.assertEquals(longTree.counter(), retrievedLongTree.counter());

        // KMemoryChunk
        KMemoryChunk chunk = new OffHeapMemoryChunk();

    }
}
