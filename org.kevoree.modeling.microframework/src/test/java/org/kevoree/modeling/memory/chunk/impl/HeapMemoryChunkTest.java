package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;

public class HeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KObjectChunk createKMemoryChunk() {
        return new HeapObjectChunk(-1,-1,-1,null);
    }
}
