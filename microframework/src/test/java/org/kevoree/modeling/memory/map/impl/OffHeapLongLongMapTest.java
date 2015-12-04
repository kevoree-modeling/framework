package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongLongMap;
import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;

/** @ignore ts */
public class OffHeapLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KLongLongMap createKLongLongMap() {
        return new OffHeapLongLongMap(new OffHeapChunkSpace(), 0, 0, 0);
    }
}
