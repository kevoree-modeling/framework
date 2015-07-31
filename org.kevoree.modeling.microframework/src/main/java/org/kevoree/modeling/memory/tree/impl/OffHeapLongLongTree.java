package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.storage.MemoryElementTypes;
import org.kevoree.modeling.memory.tree.KLongLongTree;

/**
 * @ignore ts
 */
public class OffHeapLongLongTree extends AbstractOffHeapTree implements KLongLongTree {

    public OffHeapLongLongTree() {
        super();
        NODE_SIZE = 6;
    }

    @Override
    public long previousOrEqualValue(long p_key) {
        long result = internal_previousOrEqual_index(p_key);
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
    public void insert(long key, long value) {
        internal_insert(key, value);
    }

    @Override
    public short type() {
        return MemoryElementTypes.LONG_LONG_TREE;
    }
}
