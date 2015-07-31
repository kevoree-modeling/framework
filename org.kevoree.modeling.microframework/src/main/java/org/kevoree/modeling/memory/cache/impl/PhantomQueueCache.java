package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.memory.storage.KMemoryStorage;

public class PhantomQueueCache extends AbstractCountingCache {

    public PhantomQueueCache(KMemoryStorage p_storage) {
        super(p_storage);
    }

}
