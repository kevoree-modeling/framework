package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.map.BaseKLongLongHashMapTest;
import org.kevoree.modeling.memory.map.KLongLongMap;

public class ArrayLongLongHashMapTest extends BaseKLongLongHashMapTest {

    @Override
    public KLongLongMap createKLongLongMap(int p_initalCapacity, float p_loadFactor) {
        return new ArrayLongLongMap(p_initalCapacity, p_loadFactor);
    }
}
