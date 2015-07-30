package org.kevoree.modeling.memory.storage.impl;

import org.kevoree.modeling.memory.storage.BaseKMemoryStorageTest;
import org.kevoree.modeling.memory.storage.KMemoryStorage;

public class ArrayMemoryStorageTest extends BaseKMemoryStorageTest {

    @Override
    public KMemoryStorage createKCache() {
        return new ArrayMemoryMemoryStorage(null);
    }
}
