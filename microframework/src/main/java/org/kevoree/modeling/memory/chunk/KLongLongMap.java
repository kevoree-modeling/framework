package org.kevoree.modeling.memory.chunk;

import org.kevoree.modeling.memory.KChunk;

import java.util.concurrent.atomic.AtomicInteger;

public interface KLongLongMap extends KChunk {

    int metaClassIndex();
    
    boolean tokenCompareAndSwap(int previous, int next);

    boolean contains(long key);

    long get(long key);

    void put(long key, long value);

    void remove(long key);

    void each(KLongLongMapCallBack callback);

    int size();

    void clear();

    long magic();

}
