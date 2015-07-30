package org.kevoree.modeling.memory.storage;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.meta.KMetaModel;

public interface KMemoryStorage {

    KMemoryElement get(long universe, long time, long obj);

    void putAndReplace(long universe, long time, long obj, KMemoryElement payload);

    KMemoryElement getOrPut(long universe, long time, long obj, KMemoryElement payload);

    KContentKey[] dirtyKeys();

    void clear(KMetaModel metaModel);

    void clean(KMetaModel metaModel);

    int size();

    void delete(KMetaModel metaModel);

}
