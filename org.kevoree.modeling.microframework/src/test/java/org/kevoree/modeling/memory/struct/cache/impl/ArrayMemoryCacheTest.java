package org.kevoree.modeling.memory.struct.cache.impl;

import org.kevoree.modeling.memory.struct.cache.BaseKCacheTest;
import org.kevoree.modeling.memory.struct.cache.KCache;


public class ArrayMemoryCacheTest extends BaseKCacheTest {

    @Override
    public KCache createKCache() {
        return new ArrayMemoryCache();
    }
}
