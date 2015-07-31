package org.kevoree.modeling.memory.cache;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaModel;

public interface KCache {

    KMemoryElement getAndMark(long universe, long time, long obj);

    void unmark(long universe, long time, long obj);

    KMemoryElement unsafeGet(long universe, long time, long obj);

    KMemoryElement createAndMark(long universe, long time, long obj, short type);

    void unMarkMemoryElement(KMemoryElement element);

    KMemoryChunk cloneMarkAndUnmark(KMemoryChunk previous, long newUniverse, long newTime, long obj, KMetaModel metaModel);

}
