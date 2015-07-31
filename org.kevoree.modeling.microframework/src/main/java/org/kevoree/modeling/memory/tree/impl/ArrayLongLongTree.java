package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.memory.tree.KLongLongTree;

import java.util.concurrent.atomic.AtomicReference;

public class ArrayLongLongTree extends AbstractArrayTree implements KLongLongTree {

    private KMemoryElement _next;

    public ArrayLongLongTree() {
        super();
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
        return KMemoryElementTypes.LONG_LONG_TREE;
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
