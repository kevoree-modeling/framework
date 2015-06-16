package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class MemorySegmentResolutionTrace implements KMemorySegmentResolutionTrace {

    private long _universe;

    private long _time;

    private KUniverseOrderMap _universeOrder;

    private KLongTree _timeTree;

    private KMemorySegment _segment;

    @Override
    public long getUniverse() {
        return this._universe;
    }

    @Override
    public void setUniverse(long p_universe) {
        this._universe = p_universe;
    }

    @Override
    public long getTime() {
        return this._time;
    }

    @Override
    public void setTime(long p_time) {
        this._time = p_time;
    }

    @Override
    public KUniverseOrderMap getUniverseTree() {
        return this._universeOrder;
    }

    @Override
    public void setUniverseOrder(KUniverseOrderMap p_u_tree) {
        this._universeOrder = p_u_tree;
    }

    @Override
    public KLongTree getTimeTree() {
        return this._timeTree;
    }

    @Override
    public void setTimeTree(KLongTree p_t_tree) {
        this._timeTree = p_t_tree;
    }

    @Override
    public KMemorySegment getSegment() {
        return this._segment;
    }

    @Override
    public void setSegment(KMemorySegment p_segment) {
        this._segment = p_segment;
    }

}
