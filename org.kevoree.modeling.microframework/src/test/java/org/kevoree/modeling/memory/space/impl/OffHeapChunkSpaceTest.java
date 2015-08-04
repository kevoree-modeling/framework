package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.space.BaseKChunkSpaceTest;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class OffHeapChunkSpaceTest extends BaseKChunkSpaceTest {

    @Override
    public KChunkSpace createKChunkSpace() {
        return new OffHeapChunkSpace();
    }


}
