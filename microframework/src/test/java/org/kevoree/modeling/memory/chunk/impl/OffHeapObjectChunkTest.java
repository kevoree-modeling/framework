package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKObjectChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.strategy.impl.OffHeapMemoryStrategy;

/**
 * @ignore ts
 */
public class OffHeapObjectChunkTest extends BaseKObjectChunkTest {

    @Override
    public KObjectChunk createKObjectChunk() {
        return new OffHeapObjectChunk(null, -1, -1, -1);
    }

    @Override
    public KInternalDataManager createKInternalDataManger() {
        return new DataManagerBuilder().withMemoryStrategy(new OffHeapMemoryStrategy()).build();
    }
}