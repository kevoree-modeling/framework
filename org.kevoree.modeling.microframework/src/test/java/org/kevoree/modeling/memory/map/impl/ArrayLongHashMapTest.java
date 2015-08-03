package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.chunk.impl.ArrayLongMap;
import org.kevoree.modeling.memory.map.BaseKLongHashMapTest;
import org.kevoree.modeling.memory.chunk.KLongMap;

public class ArrayLongHashMapTest extends BaseKLongHashMapTest {

    @Override
    public KLongMap createKLongHashMap(int p_initalCapacity, float p_loadFactor) {
        return new ArrayLongMap<String>(p_initalCapacity, p_loadFactor);
    }
}
