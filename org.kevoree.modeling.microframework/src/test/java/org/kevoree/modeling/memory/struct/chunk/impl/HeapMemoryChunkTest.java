package org.kevoree.modeling.memory.struct.chunk.impl;


import org.kevoree.modeling.memory.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.chunk.impl.HeapMemoryChunk;

public class HeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KMemoryChunk createKMemoryChunk() {
        return new HeapMemoryChunk();
    }

}
