package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.map.BaseKLongLongMapTest;
import org.kevoree.modeling.memory.chunk.KLongLongMap;

public class ArrayLongLongMapTest extends BaseKLongLongMapTest {

    @Override
    public KLongLongMap createKLongLongMap() {
        return new ArrayLongLongMap(-1,-1,-1,null);
    }

}
