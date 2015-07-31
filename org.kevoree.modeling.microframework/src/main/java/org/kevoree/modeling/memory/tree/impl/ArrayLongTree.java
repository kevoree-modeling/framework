package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.tree.KLongTree;

import java.util.concurrent.atomic.AtomicReference;

public class ArrayLongTree extends AbstractArrayTree implements KLongTree {

    private KMemoryElement _next;

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

    @Override
    public short type() {
        return 0;
    }

    @Override
    public KMemoryElement next() {
        return _next;
    }

    @Override
    public void insertInto(AtomicReference<KMemoryElement> list){
        // assert next == null;
        do {
            _next = list.get();
        } while (list.compareAndSet(_next, this));
    }
}
