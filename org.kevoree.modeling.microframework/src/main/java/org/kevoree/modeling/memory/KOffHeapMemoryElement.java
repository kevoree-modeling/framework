package org.kevoree.modeling.memory;

public interface KOffHeapMemoryElement extends KMemoryElement {

    long getMemoryAddress();

    void setMemoryAddress(long address);

}
