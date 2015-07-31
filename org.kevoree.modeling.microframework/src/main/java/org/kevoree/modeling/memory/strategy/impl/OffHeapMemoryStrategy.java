package org.kevoree.modeling.memory.strategy.impl;

import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.cache.impl.PhantomQueueCache;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.storage.impl.OffHeapMemoryStorage;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;

/**
 * @ignore ts
 */
public class OffHeapMemoryStrategy implements KMemoryStrategy {

    @Override
    public KMemoryStorage newStorage() {
        return new OffHeapMemoryStorage();
    }

    @Override
    public KCache newCache(KMemoryStorage p_storage) {
        return new PhantomQueueCache(p_storage);
    }

}