package org.kevoree.modeling.memory.cache;

import org.kevoree.modeling.memory.KMemoryElement;

public interface KCache {

    KMemoryElement getAndMark(long universe, long time, long obj);

    void unmark(long universe, long time, long obj);

    KMemoryElement unsafeGet(long universe, long time, long obj);

    KMemoryElement createAndMark(long universe, long time, long obj);

    void unMarkMemoryElement(KMemoryElement element);

    KMemoryElement cloneMarkAndUnmark(KMemoryElement previous, long universe, long time, long obj, long newUniverse, long newTime);


}
