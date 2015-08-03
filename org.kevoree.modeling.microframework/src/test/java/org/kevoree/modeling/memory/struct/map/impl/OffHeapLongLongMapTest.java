package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.impl.OffHeapLongLongMap;


public class OffHeapLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KLongLongMap createKUniverseOrderMap() {
        return new OffHeapLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

}
