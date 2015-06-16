package org.kevoree.modeling.memory.struct;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.KMemoryFactory;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.segment.impl.OffHeapMemorySegment;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.memory.struct.tree.impl.ArrayLongLongTree;
import org.kevoree.modeling.memory.struct.tree.impl.OffHeapLongTree;

public class OffHeapMemoryFactory implements KMemoryFactory {

    @Override
    public KMemorySegment newCacheSegment() {
        return new OffHeapMemorySegment();
    }

    @Override
    public KLongTree newLongTree() {
        return new OffHeapLongTree();
    }

    @Override
    public KLongLongTree newLongLongTree() {
        return new ArrayLongLongTree();
    }

    @Override
    public KUniverseOrderMap newUniverseMap(int initSize, String className) {
        return null;
    }

    @Override
    public KMemoryElement newFromKey(long universe, long time, long uuid) {
        if (universe != KConfig.NULL_LONG && time != KConfig.NULL_LONG && uuid != KConfig.NULL_LONG) {
            return newCacheSegment();
        }
        return null;
    }

}
