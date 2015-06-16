package org.kevoree.modeling.memory.struct;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayUniverseOrderMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.KMemoryFactory;
import org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.memory.struct.tree.impl.LongLongTree;
import org.kevoree.modeling.memory.struct.tree.impl.LongTree;

public class HeapMemoryFactory implements KMemoryFactory {

    @Override
    public KMemorySegment newCacheSegment() {
        return new HeapMemorySegment();
    }

    @Override
    public KLongTree newLongTree() {
        return new LongTree();
    }

    @Override
    public KLongLongTree newLongLongTree() {
        return new LongLongTree();
    }

    @Override
    public KUniverseOrderMap newUniverseMap(int initSize, String p_className) {
        return new ArrayUniverseOrderMap(initSize, KConfig.CACHE_LOAD_FACTOR, p_className);
    }

    @Override
    public KMemoryElement newFromKey(long universe, long time, long uuid) {
        KMemoryElement result;
        boolean isUniverseNotNull = universe != KConfig.NULL_LONG;
        if (KConfig.END_OF_TIME == uuid) {
            if (isUniverseNotNull) {
                result = newLongLongTree();
            } else {
                result = newUniverseMap(0, null);
            }
        } else {
            boolean isTimeNotNull = time != KConfig.NULL_LONG;
            boolean isObjNotNull = uuid != KConfig.NULL_LONG;
            if (isUniverseNotNull && isTimeNotNull && isObjNotNull) {
                result = newCacheSegment();
            } else if (isUniverseNotNull && !isTimeNotNull && isObjNotNull) {
                result = newLongTree();
            } else {
                result = newUniverseMap(0, null);
            }
        }
        return result;
    }

}
