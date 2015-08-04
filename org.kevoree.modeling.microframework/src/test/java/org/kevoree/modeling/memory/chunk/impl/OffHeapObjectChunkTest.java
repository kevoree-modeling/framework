package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKObjectChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;
import org.kevoree.modeling.memory.strategy.impl.OffHeapMemoryStrategy;

/**
 * @ignore ts
 */
public class OffHeapObjectChunkTest extends BaseKObjectChunkTest {

    @Override
    public KObjectChunk createKObjectChunk() {
        return new OffHeapObjectChunk(new OffHeapChunkSpace(), 0, 0, 0);
    }

    @Override
    public KInternalDataManager createKInternalDataManger() {
        return new DataManagerBuilder().withMemoryStrategy(new OffHeapMemoryStrategy()).build();
    }
}
