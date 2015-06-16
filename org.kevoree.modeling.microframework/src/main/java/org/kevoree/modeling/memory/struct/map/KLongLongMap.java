package org.kevoree.modeling.memory.struct.map;

public interface KLongLongMap {

    boolean contains(long key);

    long get(long key);

    void put(long key, long value);

    void each(KLongLongMapCallBack callback);

    int size();

    void clear();
}
