package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.storage.KMemoryStorage;

public class PhantomQueueCache extends AbstractCountingCache {

    public PhantomQueueCache(KMemoryStorage p_storage) {
        super(p_storage);
    }

    @Override
    public void register(KObject object) {

    }

    @Override
    public void registerAll(KObject[] objects) {

    }
}
