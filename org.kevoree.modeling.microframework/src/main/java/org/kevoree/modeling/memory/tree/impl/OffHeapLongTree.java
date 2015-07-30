package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.tree.KLongTree;

/**
 * @ignore ts
 */
public class OffHeapLongTree extends AbstractOffHeapTree implements KLongTree, KOffHeapMemoryElement {

    public OffHeapLongTree() {
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
}
