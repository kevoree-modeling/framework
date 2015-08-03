package org.kevoree.modeling.memory.map;

import org.kevoree.modeling.memory.KMemoryElement;

public interface KLongLongMap extends KMemoryElement {

    int metaClassIndex();

    boolean contains(long key);

    long get(long key);

    void put(long key, long value);

    void remove(long key);

    void each(KLongLongMapCallBack callback);

    int size();

    void clear();

}
