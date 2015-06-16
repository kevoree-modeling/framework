package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KTimeWalker;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.memory.manager.impl.HeapMemoryManager;
import org.kevoree.modeling.memory.manager.impl.ResolutionHelper;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;

public class AbstractTimeWalker implements KTimeWalker {

    public AbstractTimeWalker(AbstractKObject p_origin) {
        this._origin = p_origin;
    }

    private AbstractKObject _origin = null;

    //TODO check the correct usage of start and end regarding multi universe
    private void internal_times(final long start, final long end, KCallback<long[]> cb) {
        KContentKey[] keys = new KContentKey[2];
        keys[0] = KContentKey.createGlobalUniverseTree();
        keys[1] = KContentKey.createUniverseTree(_origin.uuid());
        final HeapMemoryManager manager = (HeapMemoryManager) _origin._manager;
        manager.bumpKeysToCache(keys, new KCallback<KMemoryElement[]>() {
            @Override
            public void on(KMemoryElement[] kMemoryElements) {
                final KUniverseOrderMap objUniverse = (KUniverseOrderMap) kMemoryElements[1];
                if (kMemoryElements[0] == null || kMemoryElements[1] == null) {
                    cb.on(null);
                } else {
                    final long[] collectedUniverse = ResolutionHelper.universeSelectByRange((KUniverseOrderMap) kMemoryElements[0], (KUniverseOrderMap) kMemoryElements[1], start, end, _origin.universe());
                    KContentKey[] timeTreeToLoad = new KContentKey[collectedUniverse.length];
                    for (int i = 0; i < collectedUniverse.length; i++) {
                        timeTreeToLoad[i] = KContentKey.createTimeTree(collectedUniverse[i], _origin.uuid());
                    }
                    manager.bumpKeysToCache(timeTreeToLoad, new KCallback<KMemoryElement[]>() {
                        @Override
                        public void on(KMemoryElement[] timeTrees) {
                            ArrayLongLongMap collector = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                            long previousDivergenceTime = end;
                            for (int i = 0; i < collectedUniverse.length; i++) {
                                KLongTree timeTree = (KLongTree) timeTrees[i];
                                if (timeTree != null) {
                                    long currentDivergenceTime = objUniverse.get(collectedUniverse[i]);
                                    final int finalI = i;
                                    final long finalPreviousDivergenceTime = previousDivergenceTime;
                                    timeTree.range(currentDivergenceTime, previousDivergenceTime, new KTreeWalker() {
                                        @Override
                                        public void elem(long t) {
                                            if (collector.size() == 0) {
                                                collector.put(collector.size(), t);
                                            } else {
                                                if (t != finalPreviousDivergenceTime) {
                                                    collector.put(collector.size(), t);
                                                }
                                            }
                                        }
                                    });
                                    previousDivergenceTime = currentDivergenceTime;
                                }
                            }
                            long[] orderedTime = new long[collector.size()];
                            for (int i = 0; i < collector.size(); i++) {
                                orderedTime[i] = collector.get(i);
                            }
                            cb.on(orderedTime);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void allTimes(KCallback<long[]> cb) {
        internal_times(KConfig.BEGINNING_OF_TIME, KConfig.END_OF_TIME, cb);
    }

    @Override
    public void timesBefore(long endOfSearch, KCallback<long[]> cb) {
        internal_times(KConfig.BEGINNING_OF_TIME, endOfSearch, cb);
    }

    @Override
    public void timesAfter(long beginningOfSearch, KCallback<long[]> cb) {
        internal_times(beginningOfSearch, KConfig.END_OF_TIME, cb);
    }

    @Override
    public void timesBetween(long beginningOfSearch, long endOfSearch, KCallback<long[]> cb) {
        internal_times(beginningOfSearch, endOfSearch, cb);
    }

}