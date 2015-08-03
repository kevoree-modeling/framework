package org.kevoree.modeling.memory.chunk;

public interface KLongTree extends KTree {

    void insert(long key);

    long previousOrEqual(long key);

    long lookup(long key);

    void range(long startKey, long endKey, KTreeWalker walker);
    
}
