package org.kevoree.modeling.memory.chunk;

import org.kevoree.modeling.memory.KChunk;

public interface KStringLongMap extends KChunk {

    boolean contains(String key);

    long get(String key);

    void put(String key, long value);

    void each(KStringLongMapCallBack callback);

    int size();

    void clear();

    void remove(String key);

}
