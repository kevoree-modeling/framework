package org.kevoree.modeling.memory.chunk;

public interface KLongLongTree extends KTree {

    void insert(long key, long value);

    long previousOrEqualValue(long key);

    long lookupValue(long key);

}
