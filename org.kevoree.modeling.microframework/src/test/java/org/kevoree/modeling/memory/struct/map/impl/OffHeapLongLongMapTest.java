package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.OffHeapUniverseOrderMap;

public class OffHeapLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KUniverseOrderMap createKUniverseOrderMap() {
        return new OffHeapUniverseOrderMap();
    }

}
