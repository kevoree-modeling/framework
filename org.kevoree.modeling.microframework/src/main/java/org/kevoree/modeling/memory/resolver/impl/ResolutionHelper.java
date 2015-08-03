package org.kevoree.modeling.memory.resolver.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.impl.ArrayLongLongMap;

public class ResolutionHelper {

    /*
    public static MemorySegmentResolutionTrace resolve_trees(long universe, long time, long uuid, KMemoryStorage cache) {
        MemorySegmentResolutionTrace result = new MemorySegmentResolutionTrace();
        KUniverseOrderMap objectUniverseTree = (KUniverseOrderMap) cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid);
        KUniverseOrderMap globalUniverseOrder = (KUniverseOrderMap) cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        result.setUniverseOrder(objectUniverseTree);
        long resolvedUniverse = resolve_universe(globalUniverseOrder, objectUniverseTree, time, universe);
        result.setUniverse(resolvedUniverse);
        KLongTree timeTree = (KLongTree) cache.get(resolvedUniverse, KConfig.NULL_LONG, uuid);
        if (timeTree != null) {
            result.setTimeTree(timeTree);
            long resolvedTime = timeTree.previousOrEqual(time);
            result.setTime(resolvedTime);
            result.setSegment((KMemoryChunk) cache.get(resolvedUniverse, resolvedTime, uuid));
        }
        return result;
    }*/



}
