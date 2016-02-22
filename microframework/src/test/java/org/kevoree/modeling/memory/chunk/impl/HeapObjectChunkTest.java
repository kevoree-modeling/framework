package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKObjectChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.space.impl.ManualChunkSpaceManager;
import org.kevoree.modeling.memory.space.impl.press.PressHeapChunkSpace;
import org.kevoree.modeling.memory.space.impl.press.PressOffHeapChunkSpace;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class HeapObjectChunkTest extends BaseKObjectChunkTest {

    @Override
    public KObjectChunk createKObjectChunk() {
        return new HeapObjectChunk(-1,-1,-1,null);
    }

    @Override
    public KInternalDataManager createKInternalDataManger() {
        return DataManagerBuilder.create().withSpace(new PressHeapChunkSpace(100000, 10)).withSpaceManager(new ManualChunkSpaceManager()).withScheduler(new DirectScheduler()).build();
    }
}
