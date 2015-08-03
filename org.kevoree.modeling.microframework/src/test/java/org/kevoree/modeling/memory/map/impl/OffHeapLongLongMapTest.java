package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongLongMap;
import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;

public class OffHeapLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KLongLongMap createKLongLongMap() {
        return new OffHeapLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }
}
