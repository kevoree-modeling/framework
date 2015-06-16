package org.kevoree.modeling.memory.struct.map;

public interface KLongMap<V> {

    boolean contains(long key);

    V get(long key);

    void put(long key, V value);

    void each(KLongMapCallBack<V> callback);

    int size();

    void clear();

}
