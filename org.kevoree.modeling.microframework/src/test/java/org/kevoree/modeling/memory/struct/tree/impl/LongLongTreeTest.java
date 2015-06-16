package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.memory.struct.tree.BaseKLongLongTreeTest;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

/**
 * Created by duke on 01/12/14.
 */
public class LongLongTreeTest extends BaseKLongLongTreeTest {

    @Override
    public KLongLongTree createKLongLongTree() {
        return new ArrayLongLongTree();
    }
}
