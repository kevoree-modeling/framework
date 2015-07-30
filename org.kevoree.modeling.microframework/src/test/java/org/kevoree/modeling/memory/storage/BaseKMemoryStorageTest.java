package org.kevoree.modeling.memory.storage;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;

public abstract class BaseKMemoryStorageTest {

    public abstract KMemoryStorage createKMemoryStorage();

    public abstract KLongLongTree createKLongLongTree();

    public abstract KLongTree createKLongTree();

    public abstract KUniverseOrderMap createKUniverseOrderMap();

    public abstract KMemoryChunk createKMemoryChunk();

    @Test
    public void test() {
        KMemoryStorage storage = createKMemoryStorage();

        // KUniverseOrderMap
        KUniverseOrderMap map = createKUniverseOrderMap();
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
        KLongLongTree longLongTree = createKLongLongTree();
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
        KLongTree longTree = createKLongTree();
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
        KMemoryChunk chunk = createKMemoryChunk();
        storage.putAndReplace(0, 0, 0, chunk);
        KMemoryChunk retrievedChunk = (KMemoryChunk) storage.get(0, 0, 0);
        Assert.assertNotNull(retrievedChunk);
    }
}

