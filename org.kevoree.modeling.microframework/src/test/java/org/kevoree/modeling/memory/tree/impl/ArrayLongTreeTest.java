package org.kevoree.modeling.memory.tree.impl;


import org.kevoree.modeling.memory.chunk.impl.ArrayLongTree;
import org.kevoree.modeling.memory.tree.BaseKLongTreeTest;
import org.kevoree.modeling.memory.chunk.KLongTree;

public class ArrayLongTreeTest extends BaseKLongTreeTest {

    @Override
    public KLongTree createKLongTree() {
        return new ArrayLongTree();
    }
}


