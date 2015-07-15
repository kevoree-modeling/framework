package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.memory.struct.tree.BaseKLongLongTreeTest;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;

/** @ignore ts*/
public class OffHeapLongLongTreeTest extends BaseKLongLongTreeTest {

    @Override
    public KLongLongTree createKLongLongTree() {
        return new OffHeapLongLongTree3();
    }
}
