package org.kevoree.modeling.memory;

import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;

public interface KOffHeapChunk extends KChunk {

    long getMemoryAddress();

    void setMemoryAddress(long address);

    void setStorage(OffHeapChunkSpace storage, long universe, long time, long obj);
}
