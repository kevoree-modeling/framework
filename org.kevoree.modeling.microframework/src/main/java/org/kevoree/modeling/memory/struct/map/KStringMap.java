package org.kevoree.modeling.memory.struct.map;

public interface KStringMap<V> {

    boolean contains(String key);

    V get(String key);

    void put(String key, V value);

    void each(KStringMapCallBack<V> callback);

    int size();

    void clear();

    void remove(String key);

}
