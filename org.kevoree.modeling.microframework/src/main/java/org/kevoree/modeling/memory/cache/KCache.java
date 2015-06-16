package org.kevoree.modeling.memory.cache;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.cache.impl.KCacheDirty;
import org.kevoree.modeling.meta.KMetaModel;

public interface KCache {

    KMemoryElement get(long universe, long time, long obj);

    void put(long universe, long time, long obj, KMemoryElement payload);

    KCacheDirty[] dirties();

    void clear(KMetaModel metaModel);

    void clean(KMetaModel metaModel);

    void monitor(KObject origin);

    int size();

}
