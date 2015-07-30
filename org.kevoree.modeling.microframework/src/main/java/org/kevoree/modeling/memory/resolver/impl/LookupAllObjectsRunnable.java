package org.kevoree.modeling.memory.resolver.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.tree.KLongTree;

public class LookupAllObjectsRunnable implements Runnable {

    private final long _universe;
    private final long _time;
    private final long[] _keys;
    private final KCallback<KObject[]> _callback;
    private final DistortedTimeResolver _resolver;


    public LookupAllObjectsRunnable(long p_universe, long p_time, long[] p_keys, KCallback<KObject[]> p_callback, DistortedTimeResolver p_store) {
        this._universe = p_universe;
        this._time = p_time;
        this._keys = p_keys;
        this._callback = p_callback;
        this._resolver = p_store;
    }

    @Override
    public void run() {

    }
}
