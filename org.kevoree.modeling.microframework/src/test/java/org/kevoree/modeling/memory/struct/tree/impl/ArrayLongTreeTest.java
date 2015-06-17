package org.kevoree.modeling.memory.struct.tree.impl;


import org.kevoree.modeling.memory.struct.tree.BaseKLongTreeTest;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class ArrayLongTreeTest extends BaseKLongTreeTest {

    @Override
    public KLongTree createKLongTree() {
        return new ArrayLongTree();
    }
}


