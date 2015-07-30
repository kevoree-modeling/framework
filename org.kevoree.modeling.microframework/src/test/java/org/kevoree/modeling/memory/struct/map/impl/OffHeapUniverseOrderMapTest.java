package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.map.BaseKUniverseOrderMapTest;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.OffHeapUniverseOrderMap;

public class OffHeapUniverseOrderMapTest extends BaseKUniverseOrderMapTest {

    @Override
    public KUniverseOrderMap createKUniverseOrderMap(int p_initalCapacity, float p_loadFactor, String p_className) {
        return new OffHeapUniverseOrderMap(p_initalCapacity, p_loadFactor, p_className);
    }

}
