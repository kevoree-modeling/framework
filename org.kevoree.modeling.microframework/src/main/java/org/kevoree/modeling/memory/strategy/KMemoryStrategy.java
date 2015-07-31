package org.kevoree.modeling.memory.strategy;

import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.storage.KMemoryStorage;

public interface KMemoryStrategy {

    KMemoryStorage newStorage();

    KCache newCache(KMemoryStorage storage);

}
