package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.cache.impl.AbstractCountingCache;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.storage.impl.HeapMemoryStorage;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;

public class HeapPhantomQueueMemoryStrategy implements KMemoryStrategy {

    @Override
    public KMemoryStorage newStorage() {
        return new HeapMemoryStorage();
    }

    @Override
    public KCache newCache(KMemoryStorage p_storage) {
        return new AbstractCountingCache(p_storage);
    }

}
