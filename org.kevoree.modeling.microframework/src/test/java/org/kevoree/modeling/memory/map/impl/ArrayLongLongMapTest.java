package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;
import org.kevoree.modeling.memory.chunk.KLongLongMap;

public class ArrayLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KLongLongMap createKLongLongMap() {
        return new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

}
