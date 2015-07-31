package org.kevoree.modeling.memory;

import org.kevoree.modeling.memory.storage.impl.OffHeapMemoryStorage;

public interface KOffHeapMemoryElement extends KMemoryElement {

    long getMemoryAddress();

    void setMemoryAddress(long address);

    void setStorage(OffHeapMemoryStorage storage, long universe, long time, long obj);
}
