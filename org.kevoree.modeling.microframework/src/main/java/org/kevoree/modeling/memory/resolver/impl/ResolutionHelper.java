package org.kevoree.modeling.memory.resolver.impl;

public class ResolutionHelper {

    /*
    public static MemorySegmentResolutionTrace resolve_trees(long universe, long time, long uuid, KChunkSpace cache) {
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
            result.setSegment((KObjectChunk) cache.get(resolvedUniverse, resolvedTime, uuid));
        }
        return result;
    }*/



}
