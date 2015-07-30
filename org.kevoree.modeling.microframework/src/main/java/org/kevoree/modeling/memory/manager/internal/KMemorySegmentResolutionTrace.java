package org.kevoree.modeling.memory.manager.internal;

import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.tree.KLongTree;

public interface KMemorySegmentResolutionTrace {

    long getUniverse();

    void setUniverse(long universe);

    long getTime();

    void setTime(long time);

    KUniverseOrderMap getUniverseTree();

    void setUniverseOrder(KUniverseOrderMap orderMap);

    KLongTree getTimeTree();

    void setTimeTree(KLongTree tree);

    KMemoryChunk getSegment();

    void setSegment(KMemoryChunk tree);

}
