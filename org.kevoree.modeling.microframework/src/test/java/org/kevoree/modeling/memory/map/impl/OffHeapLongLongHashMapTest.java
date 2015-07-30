package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.map.BaseKLongLongHashMapTest;
import org.kevoree.modeling.memory.map.KLongLongMap;

public class OffHeapLongLongHashMapTest extends BaseKLongLongHashMapTest {

    @Override
    public KLongLongMap createKLongLongHashMap(int p_initalCapacity, float p_loadFactor) {
        return new OffHeapLongLongMap(p_initalCapacity, p_loadFactor);
    }
}
