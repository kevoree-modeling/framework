package org.kevoree.modeling.memory.tree.impl;

import org.kevoree.modeling.memory.chunk.KLongTree;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongTree;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;
import org.kevoree.modeling.memory.tree.BaseKLongTreeTest;

/**
 * @ignore ts
 */

public class OffHeapLongTreeTest extends BaseKLongTreeTest {

    @Override
    public KLongTree createKLongTree() {
        return new OffHeapLongTree(-1, -1, -1, -1, null);
    }
}
