package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;

public class HeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KMemoryChunk createKMemorySegment() {
        return new HeapMemoryChunk();
    }

}
