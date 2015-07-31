package org.kevoree.modeling.memory.storage;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaModel;

public interface KMemoryStorage {

    KMemoryElement get(long universe, long time, long obj);

    KMemoryElement create(long universe, long time, long obj, short type);

    KMemoryChunk clone(KMemoryChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel);

    void clear(KMetaModel metaModel);

    void delete(KMetaModel metaModel);

    void remove(long universe, long time, long obj, KMetaModel metaModel);

    int size();

    void compact();
}
