package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class ArrayLongTree extends AbstractArrayTree implements KLongTree {

    public long previousOrEqual(long key) {
        int result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    public void insert(long p_key) {
        internal_insert(p_key,p_key);
    }

}
