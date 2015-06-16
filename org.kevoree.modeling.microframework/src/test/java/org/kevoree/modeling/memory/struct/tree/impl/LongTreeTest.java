package org.kevoree.modeling.memory.struct.tree.impl;


import org.kevoree.modeling.memory.struct.tree.BaseKLongTreeTest;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class LongTreeTest extends BaseKLongTreeTest {

    @Override
    public KLongTree createLongTree() {
        return new LongTree();
    }
}


