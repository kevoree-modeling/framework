package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.BaseKUniverseOrderMapTest;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.OffHeapUniverseOrderMap;

public class OffHeapUniverseOrderMapTest extends BaseKUniverseOrderMapTest {

    @Override
    public KUniverseOrderMap createKUniverseOrderMap() {
        return new OffHeapUniverseOrderMap(KConfig.CACHE_INIT_SIZE,KConfig.CACHE_LOAD_FACTOR,null);
    }

}
