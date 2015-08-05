package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.memory.chunk.BaseKObjectChunkTest;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

public class HeapObjectChunkTest extends BaseKObjectChunkTest {

    @Override
    public KObjectChunk createKObjectChunk() {
        return new HeapObjectChunk(-1,-1,-1,null);
    }

    @Override
    public KInternalDataManager createKInternalDataManger() {
        return DataManagerBuilder.buildDefault();
    }
}