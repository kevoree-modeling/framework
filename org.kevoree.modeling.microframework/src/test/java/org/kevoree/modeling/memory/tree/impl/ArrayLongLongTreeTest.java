package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.memory.tree.BaseKLongLongTreeTest;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;

/**
 * Created by duke on 01/12/14.
 */
public class ArrayLongLongTreeTest extends BaseKLongLongTreeTest {

    @Override
    public KLongLongTree createKLongLongTree() {
        return new ArrayLongLongTree();
    }
}
