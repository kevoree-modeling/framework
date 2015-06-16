package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.KContentKey;

public class KCacheDirty {

    public KContentKey key;

    public KMemoryElement object;

    public KCacheDirty(KContentKey key, KMemoryElement object) {
        this.key = key;
        this.object = object;
    }
}
