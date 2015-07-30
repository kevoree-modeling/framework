package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.map.BaseKUniverseOrderMapTest;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;

public class ArrayUniverseOrderMapTest extends BaseKUniverseOrderMapTest {

    @Override
    public KUniverseOrderMap createKUniverseOrderMap(int p_initalCapacity, float p_loadFactor, String p_className) {
        return new ArrayUniverseOrderMap(p_initalCapacity, p_loadFactor, p_className);
    }
    
}
