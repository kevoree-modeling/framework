package org.kevoree.modeling.memory.cache;

import org.kevoree.modeling.memory.KMemoryElement;

public interface KCache {

    KMemoryElement getAndMark(long universe, long time, long obj);

    void unmark(long universe, long time, long obj);

    KMemoryElement unsafeGet(long universe, long time, long obj);

    KMemoryElement createAndMark(long universe, long time, long obj, short type);

    void unMarkMemoryElement(KMemoryElement element);

    KMemoryElement cloneMarkAndUnmark(KMemoryElement previous, long newUniverse, long newTime, long obj);

}
