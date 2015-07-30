package org.kevoree.modeling.memory.map;

public interface KLongLongMap {

    boolean contains(long key);

    long get(long key);

    void put(long key, long value);

    void remove(long key);

    void each(KLongLongMapCallBack callback);

    int size();

    void clear();
}
