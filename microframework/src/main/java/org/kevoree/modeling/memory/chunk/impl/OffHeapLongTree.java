package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KLongTree;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;

/**
 * @ignore ts
 */
public class OffHeapLongTree extends AbstractOffHeapTree implements KLongTree, KOffHeapChunk {

    public OffHeapLongTree(OffHeapChunkSpace p_space, long p_universe, long p_time, long p_obj) {
        super();
        NODE_SIZE = 5;
        this._space = p_space;
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;

        allocate(0);
    }

    @Override
    public void insertKey(long p_key) {
        internal_insert(p_key, p_key);
    }

    @Override
    public long previousOrEqual(long p_key) {
        long result = previousOrEqualIndex(p_key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long magic() {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public short type() {
        return KChunkTypes.LONG_TREE;
    }

}
