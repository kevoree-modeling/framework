package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import sun.misc.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapLongTree3 extends AbstractOffHeapTree3 implements KLongTree, KOffHeapMemoryElement {

    public OffHeapLongTree3() {
        super();
        NODE_SIZE = 5;
    }

    @Override
    public void insert(long key) {
        internal_insert(key, key);
    }

    @Override
    public long previousOrEqual(long key) {
        long result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        _start_address = address;

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * _loadFactor);
    }
}
