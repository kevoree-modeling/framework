package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.memory.chunk.impl.OffHeapLongLongTree;
import org.kevoree.modeling.memory.tree.BaseKLongLongTreeTest;
import org.kevoree.modeling.memory.chunk.KLongLongTree;

/** @ignore ts*/
public class OffHeapLongLongTreeTest extends BaseKLongLongTreeTest {

    @Override
    public KLongLongTree createKLongLongTree() {
        return new OffHeapLongLongTree();
    }
}
