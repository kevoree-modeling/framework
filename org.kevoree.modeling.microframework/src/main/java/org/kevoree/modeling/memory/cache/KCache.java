package org.kevoree.modeling.memory.cache;

import org.kevoree.modeling.memory.KMemoryElement;

public interface KCache {

    KMemoryElement getAndMark(long universe, long time, long obj);

    KMemoryElement getMarkAndUpdate(long universe, long time, long obj);

    void unMark(long universe, long time, long obj);

    void markAndPut(long universe, long time, long obj, KMemoryElement element);



}
