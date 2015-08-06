package org.kevoree.modeling.memory;

public interface KOffHeapChunk extends KChunk {

    long memoryAddress();

    void setMemoryAddress(long address);

}
