package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.memory.tree.BaseKLongTreeTest;
import org.kevoree.modeling.memory.tree.KLongTree;

/** @ignore ts*/

public class OffHeapLongTreeTest extends BaseKLongTreeTest {

    @Override
    public KLongTree createKLongTree() {
        return new OffHeapLongTree();
    }
}
