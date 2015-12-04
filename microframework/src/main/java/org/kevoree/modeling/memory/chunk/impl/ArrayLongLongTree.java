package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.chunk.KLongLongTree;

public class ArrayLongLongTree extends AbstractArrayTree implements KLongLongTree {

    public ArrayLongLongTree(long p_universe, long p_time, long p_obj, KChunkSpace p_space) {
        super(p_universe, p_time, p_obj, p_space);
        this.kvSize = 2;
    }

    @Override
    public long previousOrEqualValue(long p_key) {
        int result = internal_previousOrEqual_index(p_key);
        if (result != -1) {
            return value(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long lookupValue(long p_key) {
        return internal_lookup_value(p_key);
    }

    @Override
    public void insert(long p_key, long p_value) {
        internal_insert(p_key, p_value);
    }

    @Override
    public short type() {
        return -1;
        //return KChunkTypes.LONG_LONG_TREE;
    }
}
