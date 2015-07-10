package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class LookupAllTimesRunnable implements Runnable {

    private long _universe;
    private long[] _times;
    private long _uuid;
    private KCallback<KObject[]> _callback;
    private MemoryManager _store;

    public LookupAllTimesRunnable(long p_universe, long[] p_times, long p_key, KCallback<KObject[]> p_callback, MemoryManager p_store) {
        this._universe = p_universe;
        this._times = p_times;

        this._uuid = p_key;
        this._callback = p_callback;
        this._store = p_store;
    }

    @Override
    public void run() {
        _store.bumpKeyToCache(KContentKey.createUniverseTree(_uuid), new KCallback<KMemoryElement>() {
            @Override
            public void on(KMemoryElement universeIndex) {
                final KLongLongMap map = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                final KLongLongMap mapReverse = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                final int[] ii = {0};
                for (int i = 0; i < _times.length; i++) {
                    if (universeIndex != null) {
                        long resolved = ResolutionHelper.resolve_universe(_store.globalUniverseOrder(), (KUniverseOrderMap) universeIndex, _times[i], _universe);
                        long alreadyInMap = map.get(resolved);
                        if (alreadyInMap == KConfig.NULL_LONG) {
                            mapReverse.put(_times[i], ii[0]);
                            map.put(resolved, ii[0]);
                            ii[0]++;
                        } else {
                            mapReverse.put(_times[i], alreadyInMap);
                        }
                    }
                }
                KContentKey[] tempKeys = new KContentKey[map.size()];
                map.each(new KLongLongMapCallBack() {
                    @Override
                    public void on(long key, long value) {
                        tempKeys[(int) value] = KContentKey.createTimeTree(key, _uuid);
                    }
                });
                _store.bumpKeysToCache(tempKeys, new KCallback<KMemoryElement[]>() {
                    @Override
                    public void on(KMemoryElement[] timeIndexes) {
                        KContentKey[] tempKeys2 = new KContentKey[_times.length];
                        for (int i = 0; i < _times.length; i++) {
                            int reversed = (int)mapReverse.get(_times[i]);
                            KContentKey resolvedContentKey = null;
                            if (timeIndexes[reversed] != null) {
                                KLongTree cachedIndexTree = (KLongTree) timeIndexes[reversed];
                                long resolvedNode = cachedIndexTree.previousOrEqual(_times[i]);
                                if (resolvedNode != KConfig.NULL_LONG) {
                                    resolvedContentKey = KContentKey.createObject(tempKeys[reversed].universe, resolvedNode, _uuid);
                                }
                            }
                            tempKeys2[i] = resolvedContentKey;
                        }
                        _store.bumpKeysToCache(tempKeys2, new KCallback<KMemoryElement[]>() {
                            @Override
                            public void on(KMemoryElement[] cachedObjects) {
                                KObject[] proxies = new KObject[_times.length];
                                for (int i = 0; i < _times.length; i++) {
                                    if (cachedObjects[i] != null) {
                                        proxies[i] = ((AbstractKModel) _store.model()).createProxy(_universe, _times[i], _uuid, _store.model().metaModel().metaClasses()[((KMemorySegment) cachedObjects[i]).metaClassIndex()]);
                                        if (proxies[i] != null) {
                                            int reversed = (int)mapReverse.get(_times[i]);
                                            KLongTree cachedIndexTree = (KLongTree) timeIndexes[reversed];
                                            cachedIndexTree.inc();

                                            KUniverseOrderMap universeTree = (KUniverseOrderMap) universeIndex;
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
