package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.chunk.KStringLongMap;
import org.kevoree.modeling.memory.chunk.impl.HeapObjectIndexChunk;
import org.kevoree.modeling.memory.map.BaseKStringLongMapTest;

public class ArrayStringLongMapTest extends BaseKStringLongMapTest {

    @Override
    public KStringLongMap createKStringLongMap() {
        return new HeapObjectIndexChunk(-1, -1, -1, null);
    }
}
