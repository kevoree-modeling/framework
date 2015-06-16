package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.BaseKLongLongHashMapTest;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;

public class ArrayLongLongHashMapTest extends BaseKLongLongHashMapTest {

    @Override
    public KLongLongMap createKLongLongHashMap(int p_initalCapacity, float p_loadFactor) {
        return new ArrayLongLongMap(p_initalCapacity, p_loadFactor);
    }
}
