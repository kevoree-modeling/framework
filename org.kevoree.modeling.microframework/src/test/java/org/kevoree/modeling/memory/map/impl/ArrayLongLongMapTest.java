package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;
import org.kevoree.modeling.memory.map.KLongLongMap;

public class ArrayLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KLongLongMap createKUniverseOrderMap() {
        return new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

}
