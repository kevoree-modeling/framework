package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.BaseKLongHashMapTest;
import org.kevoree.modeling.memory.struct.map.KLongMap;

/**
 * Created by duke on 03/03/15.
 */
public class ArrayLongHashMapTest extends BaseKLongHashMapTest {

    @Override
    public KLongMap createKLongHashMap(int p_initalCapacity, float p_loadFactor) {
        return new ArrayLongMap<String>(p_initalCapacity, p_loadFactor);
    }
}
