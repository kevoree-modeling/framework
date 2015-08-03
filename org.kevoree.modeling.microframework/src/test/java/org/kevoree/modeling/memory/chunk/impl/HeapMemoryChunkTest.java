package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;

public class HeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KObjectChunk createKMemoryChunk() {
        return new HeapObjectChunk();
    }
}
