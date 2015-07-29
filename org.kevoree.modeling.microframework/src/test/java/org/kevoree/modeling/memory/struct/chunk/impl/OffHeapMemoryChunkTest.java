package org.kevoree.modeling.memory.struct.chunk.impl;

import org.kevoree.modeling.memory.struct.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.struct.chunk.KMemoryChunk;

/**
 * @ignore ts
 */
public class OffHeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KMemoryChunk createKMemoryChunk() {
        return new OffHeapMemoryChunk();
    }
}
