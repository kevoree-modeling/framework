package org.kevoree.modeling.memory.cache;

import org.kevoree.modeling.memory.KMemoryElement;

public interface KCache {

    KMemoryElement getAndMark(long universe, long time, long obj);

    //previous should be updated
    KMemoryElement getMarkAndUpdate(long universe, long time, long obj, long[] previous);

    void unMark(long universe, long time, long obj);

    KMemoryElement createAndMark(long universe, long time, long obj);

    void unMarkMemoryElement(KMemoryElement element);

}
