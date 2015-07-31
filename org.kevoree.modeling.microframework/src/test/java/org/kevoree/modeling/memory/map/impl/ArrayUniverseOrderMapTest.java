package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.map.BaseKUniverseOrderMapTest;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;

public class ArrayUniverseOrderMapTest extends BaseKUniverseOrderMapTest {

    @Override
    public KUniverseOrderMap createKUniverseOrderMap() {
        return new ArrayUniverseOrderMap();
    }
    
}
