package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;

/**
 * @ignore ts
 */
public class OffHeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KMemoryChunk createKMemoryChunk() {
        return new OffHeapMemoryChunk();
    }
}
