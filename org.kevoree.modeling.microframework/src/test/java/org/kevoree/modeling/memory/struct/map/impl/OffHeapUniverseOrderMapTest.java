package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.BaseKUniverseOrderMapTest;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;

public class OffHeapUniverseOrderMapTest extends BaseKUniverseOrderMapTest {

    @Override
    public KUniverseOrderMap createKUniverseOrderMap(int p_initalCapacity, float p_loadFactor, String p_className) {
        return new OffHeapUniverseOrderMap(p_initalCapacity, p_loadFactor, p_className);
    }

}
