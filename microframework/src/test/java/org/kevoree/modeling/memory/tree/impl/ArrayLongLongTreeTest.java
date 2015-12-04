package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongTree;
import org.kevoree.modeling.memory.tree.BaseKLongLongTreeTest;
import org.kevoree.modeling.memory.chunk.KLongLongTree;

public class ArrayLongLongTreeTest extends BaseKLongLongTreeTest {

    @Override
    public KLongLongTree createKLongLongTree() {
        return new ArrayLongLongTree(-1,-1,-1,null);
    }
}
