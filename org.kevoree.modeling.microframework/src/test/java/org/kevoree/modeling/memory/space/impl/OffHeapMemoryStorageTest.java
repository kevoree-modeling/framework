package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.space.BaseKMemoryStorageTest;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class OffHeapMemoryStorageTest extends BaseKMemoryStorageTest {

    @Override
    public KChunkSpace createKMemoryStorage() {
        return new OffHeapChunkSpace();
    }

}
