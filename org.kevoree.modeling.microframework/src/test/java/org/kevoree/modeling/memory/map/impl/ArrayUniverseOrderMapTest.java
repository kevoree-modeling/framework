package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.BaseKUniverseOrderMapTest;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;

public class ArrayUniverseOrderMapTest extends BaseKUniverseOrderMapTest {

    @Override
    public KLongLongMap createKUniverseOrderMap() {
        return new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.NULL_LONG);
    }

}
