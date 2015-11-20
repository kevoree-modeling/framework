package org.kevoree.modeling.memory.chunk;

public interface KStringLongMap {

    boolean contains(String key);

    long get(String key);

    void put(String key, long value);

    void each(KStringLongMapCallBack callback);

    int size();

    void clear();

    void remove(String key);

}
