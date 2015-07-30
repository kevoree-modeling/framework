package org.kevoree.modeling.memory.manager.impl;

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

    private long _universe;
    private long _time;

    private long[] _keys;
    private KCallback<KObject[]> _callback;
    private DataManager _store;

    public LookupAllObjectsRunnable(long p_universe, long p_time, long[] p_keys, KCallback<KObject[]> p_callback, DataManager p_store) {
        this._universe = p_universe;
        this._time = p_time;

        this._keys = p_keys;
        this._callback = p_callback;
        this._store = p_store;
    }

    @Override
    public void run() {
        final KContentKey[] tempKeys = new KContentKey[_keys.length];
        for (int i = 0; i < _keys.length; i++) {
            if (_keys[i] != KConfig.NULL_LONG) {
                tempKeys[i] = KContentKey.createUniverseTree(_keys[i]);
            }
        }
        _store.bumpKeysToCache(tempKeys, new KCallback<KMemoryElement[]>() {
            @Override
            public void on(KMemoryElement[] universeIndexes) {
                for (int i = 0; i < _keys.length; i++) {
                    KContentKey toLoadKey = null;
                    if (universeIndexes[i] != null) {
                        KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _store.cache().get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
                        long closestUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, (KUniverseOrderMap) universeIndexes[i], _time, _universe);
                        toLoadKey = KContentKey.createTimeTree(closestUniverse, _keys[i]);
                    }
                    tempKeys[i] = toLoadKey;
                }
                _store.bumpKeysToCache(tempKeys, new KCallback<KMemoryElement[]>() {
                    @Override
                    public void on(KMemoryElement[] timeIndexes) {
                        for (int i = 0; i < _keys.length; i++) {
                            KContentKey resolvedContentKey = null;
                            if (timeIndexes[i] != null) {
                                KLongTree cachedIndexTree = (KLongTree) timeIndexes[i];
                                long resolvedNode = cachedIndexTree.previousOrEqual(_time);
                                if (resolvedNode != KConfig.NULL_LONG) {
                                    resolvedContentKey = KContentKey.createObject(tempKeys[i].universe, resolvedNode, _keys[i]);
                                }
                            }
                            tempKeys[i] = resolvedContentKey;
                        }
                        _store.bumpKeysToCache(tempKeys, new KCallback<KMemoryElement[]>() {
                            @Override
                            public void on(KMemoryElement[] cachedObjects) {
                                KObject[] proxies = new KObject[_keys.length];
                                for (int i = 0; i < _keys.length; i++) {
                                    if (cachedObjects[i] != null) {
                                        proxies[i] = ((AbstractKModel) _store.model()).createProxy(_universe, _time, _keys[i], _store.model().metaModel().metaClasses()[((KMemoryChunk) cachedObjects[i]).metaClassIndex()]);
                                        if (proxies[i] != null) {
                                            KLongTree cachedIndexTree = (KLongTree) timeIndexes[i];
                                            cachedIndexTree.inc();

                                            KUniverseOrderMap universeTree = (KUniverseOrderMap) universeIndexes[i];
                                            universeTree.inc();

                                            cachedObjects[i].inc();
                                        }
                                    }
                                }
                                _callback.on(proxies);
                            }
                        });
                    }
                });
            }
        });
    }

}
