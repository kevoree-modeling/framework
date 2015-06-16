package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.BaseKStringHashMapTest;
import org.kevoree.modeling.memory.struct.map.KStringMap;

/**
 * Created by duke on 09/04/15.
 */
public class ArrayStringHashMapTest extends BaseKStringHashMapTest {

    @Override
    public KStringMap createKStringHashMap(int p_initalCapacity, float p_loadFactor) {
        return new ArrayStringMap<String>(p_initalCapacity, p_loadFactor);
    }
}
