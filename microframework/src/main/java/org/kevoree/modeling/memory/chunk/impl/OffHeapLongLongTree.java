package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KLongLongTree;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;

/**
 * @ignore ts
 */
public class OffHeapLongLongTree extends AbstractOffHeapTree implements KLongLongTree {

    public OffHeapLongLongTree(OffHeapChunkSpace p_space, long p_universe, long p_time, long p_obj) {
        super();

        NODE_SIZE = 6;
        this._space = p_space;
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;

        allocate(0);
    }

    @Override
    public long previousOrEqualValue(long p_key) {
        long result = previousOrEqualIndex(p_key);
        if (result != -1) {
            return value(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long lookupValue(long p_key) {
        return internal_lookupValue(p_key);
    }

    @Override
    public void insert(long p_key, long p_value) {
        internal_insert(p_key, p_value);
    }

    @Override
    public short type() {
        return -1;
    }


}
