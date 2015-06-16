package org.kevoree.modeling.memory.struct.tree;

public interface KLongLongTree extends KTree {

    void insert(long key, long value);

    long previousOrEqualValue(long key);

    long lookupValue(long key);

}
