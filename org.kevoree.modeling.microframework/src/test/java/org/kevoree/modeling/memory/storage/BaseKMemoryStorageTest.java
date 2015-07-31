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

    @Test
    public void test() {
        KMemoryStorage storage = createKMemoryStorage();

        // KUniverseOrderMap
        KUniverseOrderMap map = (KUniverseOrderMap) storage.create(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, KMemoryElementTypes.LONG_LONG_MAP);
        map.put(0, 0);
        map.put(1, 1);
        KUniverseOrderMap retrievedMap = (KUniverseOrderMap) storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        Assert.assertEquals(0, retrievedMap.get(0));
        Assert.assertEquals(1, retrievedMap.get(1));
        Assert.assertEquals(map.size(), retrievedMap.size());
        Assert.assertEquals(map.isDirty(), retrievedMap.isDirty());
        Assert.assertEquals(map.counter(), retrievedMap.counter());

        // KLongLongTree
        KLongLongTree longLongTree = (KLongLongTree) storage.create(0, KConfig.NULL_LONG, KConfig.END_OF_TIME, KMemoryElementTypes.LONG_LONG_TREE);
        longLongTree.init(null, null, -1);
        longLongTree.insert(0, 0);
        longLongTree.insert(1, 1);
        KLongLongTree retrievedLongLongTree = (KLongLongTree) storage.get(0, KConfig.NULL_LONG, KConfig.END_OF_TIME);
        Assert.assertEquals(0, retrievedLongLongTree.lookupValue(0));
        Assert.assertEquals(1, retrievedLongLongTree.lookupValue(1));
        Assert.assertEquals(longLongTree.size(), retrievedLongLongTree.size());
        Assert.assertEquals(longLongTree.isDirty(), retrievedLongLongTree.isDirty());
        Assert.assertEquals(longLongTree.counter(), retrievedLongLongTree.counter());

        // KLongTree
        KLongTree longTree = (KLongTree) storage.create(0, KConfig.NULL_LONG, 0, KMemoryElementTypes.LONG_TREE);
        longTree.init(null, null, -1);
        longTree.insert(0);
        longTree.insert(1);
        KLongTree retrievedLongTree = (KLongTree) storage.get(0, KConfig.NULL_LONG, 0);
        Assert.assertEquals(0, retrievedLongTree.lookup(0));
        Assert.assertEquals(1, retrievedLongTree.lookup(1));
        Assert.assertEquals(longTree.size(), retrievedLongTree.size());
        Assert.assertEquals(longTree.isDirty(), retrievedLongTree.isDirty());
        Assert.assertEquals(longTree.counter(), retrievedLongTree.counter());

        // KMemoryChunk
        KMemoryChunk chunk = (KMemoryChunk) storage.create(0, 0, 0, KMemoryElementTypes.CHUNK);
        KMemoryChunk retrievedChunk = (KMemoryChunk) storage.get(0, 0, 0);
        Assert.assertNotNull(retrievedChunk);
    }
}

