package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KLongTree;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.press.PressOffHeapChunkSpace;

/**
 * @ignore ts
 */
public class OffHeapLongTree extends AbstractOffHeapTree implements KLongTree, KOffHeapChunk {

    public OffHeapLongTree(long p_mem_addr, long p_universe, long p_time, long p_obj, PressOffHeapChunkSpace p_space) {
        super();
        NODE_SIZE = 5;
        this._space = p_space;
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;

        if (p_mem_addr == -1) {
            allocate(0);
        } else {
            this._start_address = p_mem_addr;
        }
    }

    @Override
    public void insertKey(long p_key) {
        internal_insert(p_key, p_key);
    }

    @Override
    public long previousOrEqual(long p_key) {
        long result = internal_previousOrEqual_index(p_key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long magic() {
        return UNSAFE.getLong(this._start_address + OFFSET)
    }

    @Override
    public short type() {
        return KChunkTypes.LONG_TREE;
    }

}
