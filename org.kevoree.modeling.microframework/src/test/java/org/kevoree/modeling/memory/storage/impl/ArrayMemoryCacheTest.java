package org.kevoree.modeling.memory.storage.impl;

import org.kevoree.modeling.memory.storage.BaseKCacheTest;
import org.kevoree.modeling.memory.storage.KMemoryStorage;

public class ArrayMemoryCacheTest extends BaseKCacheTest {

    @Override
    public KMemoryStorage createKCache() {
        return new ArrayMemoryMemoryStorage(null);
    }
}
