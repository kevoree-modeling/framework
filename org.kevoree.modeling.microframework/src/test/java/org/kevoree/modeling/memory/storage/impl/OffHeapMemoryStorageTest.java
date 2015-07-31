package org.kevoree.modeling.memory.storage.impl;

import org.kevoree.modeling.memory.storage.BaseKMemoryStorageTest;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.strategy.impl.OffHeapMemoryStrategy;

public class OffHeapMemoryStorageTest extends BaseKMemoryStorageTest {

    @Override
    public KMemoryStorage createKMemoryStorage() {
        return new OffHeapMemoryStorage();
    }

        return new OffHeapMemoryMemoryStorage(new OffHeapMemoryStrategy());
    }

}
