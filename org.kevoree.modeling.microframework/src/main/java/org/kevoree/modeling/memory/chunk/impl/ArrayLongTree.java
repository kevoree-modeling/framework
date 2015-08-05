package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.KLongTree;
import org.kevoree.modeling.memory.space.KChunkSpace;

public class ArrayLongTree extends AbstractArrayTree implements KLongTree {

    public ArrayLongTree(long p_universe, long p_time, long p_obj, KChunkSpace p_space) {
        super(p_universe, p_time, p_obj, p_space);
    }

    public long previousOrEqual(long key) {
        int result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    public void insert(long p_key) {
        internal_insert(p_key, p_key);
    }

    @Override
    public short type() {
        return 0;
    }

}
