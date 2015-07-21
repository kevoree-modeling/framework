package org.kevoree.modeling.memory.struct.cache;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.meta.KMetaModel;

public interface KCache {

    KMemoryElement get(long universe, long time, long obj);

    void putAndReplace(long universe, long time, long obj, KMemoryElement payload);

    KMemoryElement getOrPut(long universe, long time, long obj, KMemoryElement payload);

    KContentKey[] dirtyKeys();

    void clear(KMetaModel metaModel);

    void clean(KMetaModel metaModel);

    void monitor(KObject origin);

    int size();

    void delete(KMetaModel metaModel);

}
