package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKObjectChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.space.impl.press.PressOffHeapChunkSpace;

/**
 * @ignore ts
 */
public class OffHeapObjectChunkTest extends BaseKObjectChunkTest {

    @Override
    public KObjectChunk createKObjectChunk() {
        return new OffHeapObjectChunk(-1, -1, -1, -1, null);
    }

    @Override
    public KInternalDataManager createKInternalDataManger() {
        return new DataManagerBuilder().withSpace(new PressOffHeapChunkSpace(100)).build();
    }
}
