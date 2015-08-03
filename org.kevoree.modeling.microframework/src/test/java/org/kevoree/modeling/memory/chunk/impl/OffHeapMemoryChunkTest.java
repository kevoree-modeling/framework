package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKMemoryChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;

/**
 * @ignore ts
 */
public class OffHeapMemoryChunkTest extends BaseKMemoryChunkTest {

    @Override
    public KObjectChunk createKMemoryChunk() {
        return new OffHeapObjectChunk();
    }
}
