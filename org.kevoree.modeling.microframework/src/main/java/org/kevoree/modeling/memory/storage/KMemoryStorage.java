package org.kevoree.modeling.memory.storage;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.meta.KMetaModel;

public interface KMemoryStorage {

    KMemoryElement get(long universe, long time, long obj);

    KMemoryElement create(long universe, long time, long obj, short type);

    KMemoryElement clone(KMemoryElement previousElement, long newUniverse, long newTime, long newObj);

    void clear(KMetaModel metaModel);

    void clean(KMetaModel metaModel);

    int size();

    void delete(KMetaModel metaModel);

}
