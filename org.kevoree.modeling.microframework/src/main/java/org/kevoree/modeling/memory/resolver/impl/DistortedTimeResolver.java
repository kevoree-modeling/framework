package org.kevoree.modeling.memory.resolver.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.meta.KMetaClass;

public class DistortedTimeResolver implements KResolver {

    private static final int KEYS_SIZE = 3;

    private static final String OUT_OF_CACHE_MESSAGE = "KMF Error: your object is out of cache, you probably kept an old reference. Please reload it with a lookup";

    private final KCache _cache;

    private final KInternalDataManager _manager;

    private final AbstractKModel _model;

    public DistortedTimeResolver(KCache p_cache, KInternalDataManager p_manager, KModel p_model) {
        this._cache = p_cache;
        this._manager = p_manager;
        this._model = (AbstractKModel) p_model;
    }

    @Override
    public Runnable lookup(long universe, long time, long uuid, KCallback<KObject> callback) {
        return new Runnable() {
            @Override
            public void run() {
                getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KMemoryElement>() {
                    @Override
                    public void on(KMemoryElement theGlobalUniverseOrderElement) {
                        if (theGlobalUniverseOrderElement != null) {
                            getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid, new KCallback<KMemoryElement>() {
                                @Override
                                public void on(KMemoryElement theObjectUniverseOrderElement) {
                                    if (theObjectUniverseOrderElement == null) {
                                        _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                                        callback.on(null);
                                    } else {
                                        long closestUniverse = ResolutionHelper.resolve_universe((KUniverseOrderMap) theGlobalUniverseOrderElement, (KUniverseOrderMap) theObjectUniverseOrderElement, time, universe);
                                        getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, uuid, new KCallback<KMemoryElement>() {
                                            @Override
                                            public void on(KMemoryElement theObjectTimeTreeElement) {
                                                if (theObjectTimeTreeElement == null) {
                                                    _cache.unMarkMemoryElement(theObjectUniverseOrderElement);
                                                    _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                                                    callback.on(null);
                                                } else {
                                                    long closestTime = ((KLongTree) theObjectTimeTreeElement).previousOrEqual(time);
                                                    getOrLoadAndMark(closestUniverse, closestTime, uuid, new KCallback<KMemoryElement>() {
                                                        @Override
                                                        public void on(KMemoryElement theObjectChunk) {
                                                            if (theObjectChunk == null) {
                                                                _cache.unMarkMemoryElement(theObjectTimeTreeElement);
                                                                _cache.unMarkMemoryElement(theObjectUniverseOrderElement);
                                                                _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                                                                callback.on(null);
                                                            } else {
                                                                callback.on(_model.createProxy(universe, time, uuid, _model.metaModel().metaClass(((KMemoryChunk) theObjectChunk).metaClassIndex()), closestUniverse, closestTime));
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        };
    }

        /*
    @Override
    public void lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(new LookupAllTimesRunnable(universe, times, uuid, callback, this));
    }

    @Override
    public void lookupAllObjectsTimes(long universe, long[] times, long[] uuid, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(new LookupAllObjectsTimesRunnable(universe, times, uuid, callback, this));
    }
*/

    @Override
    public Runnable lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback) {
        return null;

        /*
        return new Runnable() {
            @Override
            public void run() {
                getOrLoadAndMark();


                final long[] tempKeys = new long[uuids.length * 3];
                for (int i = 0; i < uuids.length; i++) {
                    if (uuids[i] != KConfig.NULL_LONG) {
                        tempKeys[i] = KContentKey.createUniverseTree(uuids[i]);
                    }
                }
                getOrLoadAndMarkAll(tempKeys, new KCallback<KMemoryElement[]>() {
                    @Override
                    public void on(KMemoryElement[] universeIndexes) {
                        for (int i = 0; i < uuids.length; i++) {
                            KContentKey toLoadKey = null;
                            if (universeIndexes[i] != null) {
                                KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _resolver.cache().get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
                                long closestUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, (KUniverseOrderMap) universeIndexes[i], time, universe);
                                toLoadKey = KContentKey.createTimeTree(closestUniverse, uuids[i]);
                            }
                            tempKeys[i] = toLoadKey;
                        }
                        getOrLoadAndMarkAll(tempKeys, new KCallback<KMemoryElement[]>() {
                            @Override
                            public void on(KMemoryElement[] timeIndexes) {
                                for (int i = 0; i < uuids.length; i++) {
                                    KContentKey resolvedContentKey = null;
                                    if (timeIndexes[i] != null) {
                                        KLongTree cachedIndexTree = (KLongTree) timeIndexes[i];
                                        long resolvedNode = cachedIndexTree.previousOrEqual(_time);
                                        if (resolvedNode != KConfig.NULL_LONG) {
                                            resolvedContentKey = KContentKey.createObject(tempKeys[i].universe, resolvedNode, uuids[i]);
                                        }
                                    }
                                    tempKeys[i] = resolvedContentKey;
                                }
                                getOrLoadAndMarkAll(tempKeys, new KCallback<KMemoryElement[]>() {
                                    @Override
                                    public void on(KMemoryElement[] cachedObjects) {
                                        KObject[] proxies = new KObject[_keys.length];
                                        for (int i = 0; i < _keys.length; i++) {
                                            if (cachedObjects[i] != null) {
                                                proxies[i] = ((AbstractKModel) _resolver.model()).createProxy(_universe, _time, _keys[i], _resolver.model().metaModel().metaClasses()[((KMemoryChunk) cachedObjects[i]).metaClassIndex()]);
                                                if (proxies[i] != null) {
                                                    KLongTree cachedIndexTree = (KLongTree) timeIndexes[i];
                                                    cachedIndexTree.inc();

                                                    KUniverseOrderMap universeTree = (KUniverseOrderMap) universeIndexes[i];
                                                    universeTree.inc();

                                                    cachedObjects[i].inc();
                                                }
                                            }
                                        }
                                        callback.on(proxies);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };*/
    }

    @Override
    public Runnable lookupAllTimes(long universe, long[] time, long uuid, KCallback<KObject[]> callback) {
        return null;
    }

    @Override
    public Runnable lookupAllObjectsTimes(long universe, long[] time, long[] uuid, KCallback<KObject[]> callback) {
        return null;
    }

    @Override
    public KMemoryChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        return internal_chunk(universe, time, uuid, false, metaClass, previousResolution);
    }

    @Override
    public KMemoryChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        return internal_chunk(universe, time, uuid, true, metaClass, previousResolution);
    }

    //TODO optimize the worst case by reusing, by using previous universe, maybe optimize
    private KMemoryChunk internal_chunk(long universe, long requestedTime, long uuid, boolean useClosest, KMetaClass metaClass, long[] previousResolution) {
        long time = requestedTime;
        if (metaClass.temporalResolution() != 1) {
            time = time - (time % metaClass.temporalResolution());
        }
        KMemoryChunk currentEntry = (KMemoryChunk) _cache.getMarkAndUpdate(universe, time, uuid, previousResolution);
        if (currentEntry != null) {
            return currentEntry;
        }
        KUniverseOrderMap objectUniverseTree = (KUniverseOrderMap) _cache.getAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid);
        if (objectUniverseTree == null) {
            return null;
        }
        KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _cache.getAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (globalUniverseTree == null) {
            _cache.unMarkMemoryElement(objectUniverseTree);
            return null;
        }
        long resolvedUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, objectUniverseTree, time, universe);
        KLongTree timeTree = (KLongTree) _cache.getAndMark(resolvedUniverse, KConfig.NULL_LONG, uuid);
        if (timeTree == null) {
            _cache.unMarkMemoryElement(globalUniverseTree);
            _cache.unMarkMemoryElement(objectUniverseTree);
            return null;
        }
        long resolvedTime = timeTree.previousOrEqual(time);
        if (resolvedTime != KConfig.NULL_LONG) {
            boolean needTimeCopy = !useClosest && (resolvedTime != time);
            boolean needUniverseCopy = !useClosest && (resolvedUniverse != universe);
            KMemoryChunk entry = (KMemoryChunk) _cache.getMarkAndUpdate(resolvedUniverse, resolvedTime, uuid, previousResolution);
            if (entry == null) {
                _cache.unMarkMemoryElement(timeTree);
                _cache.unMarkMemoryElement(globalUniverseTree);
                _cache.unMarkMemoryElement(objectUniverseTree);
                return null;
            }
            if (!needTimeCopy && !needUniverseCopy) {
                if (!useClosest) {
                    entry.setDirty();
                }
                //TODO fill previous
                return entry;
            } else {
                KMemoryChunk clonedEntry = entry.clone(metaClass);
                clonedEntry = (KMemoryChunk) _cache.getOrPut(universe, time, uuid, clonedEntry);
                if (!needUniverseCopy) {
                    timeTree.insert(time);
                } else {
                    KLongTree newTemporalTree = _factory.newLongTree();
                    newTemporalTree.insert(time);
                    newTemporalTree.inc();
                    timeTree.dec();
                    _cache.getOrPut(universe, KConfig.NULL_LONG, uuid, newTemporalTree);
                    objectUniverseTree.put(universe, time);//insert this time as a divergence point for this object
                }
                entry.dec();
                clonedEntry.inc();
                //TODO fill previous
                return clonedEntry;
            }
        } else {
            _cache.unMarkMemoryElement(timeTree);
            _cache.unMarkMemoryElement(globalUniverseTree);
            _cache.unMarkMemoryElement(objectUniverseTree);
            return null;
        }
    }

    @Override
    public void indexObject(KObject obj) {
        int metaClassIndex = obj.metaClass().index();
        KMemoryChunk cacheEntry = (KMemoryChunk) _cache.createAndMark(obj.universe(), obj.now(), obj.uuid());
        cacheEntry.initMetaClass(obj.metaClass());
        cacheEntry.init(null, _model.metaModel(), metaClassIndex);
        cacheEntry.setDirty();
        //initiate time management
        KLongTree timeTree = (KLongTree) _cache.createAndMark(obj.universe(), KConfig.NULL_LONG, obj.uuid());
        timeTree.init(null, _model.metaModel(), metaClassIndex);
        timeTree.insert(obj.now());
        //initiate universe management
        KUniverseOrderMap universeTree = (KUniverseOrderMap) _cache.createAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, obj.uuid());
        universeTree.init(null, _model.metaModel(), metaClassIndex);
        universeTree.put(obj.universe(), obj.now());
    }

    public final void getOrLoadAndMark(long universe, long time, long uuid, final KCallback<KMemoryElement> callback) {
        KMemoryElement cached = _cache.getAndMark(universe, time, uuid);
        if (cached != null) {
            callback.on(cached);
        } else {
            _manager.load(new long[]{universe, time, uuid}, new KCallback<KMemoryElement[]>() {
                @Override
                public void on(KMemoryElement[] loadedElements) {
                    callback.on(loadedElements[0]);
                }
            });
        }
    }

    public final void getOrLoadAndMarkAll(long[] keys, final KCallback<KMemoryElement[]> callback) {
        int nbKeys = keys.length / KEYS_SIZE;
        final boolean[] toLoadIndexes = new boolean[nbKeys];
        int nbElem = 0;
        final KMemoryElement[] result = new KMemoryElement[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            result[i] = _cache.getAndMark(keys[i * KEYS_SIZE], keys[i * KEYS_SIZE + 1], keys[i * KEYS_SIZE + 2]);
            if (result[i] == null) {
                toLoadIndexes[i] = true;
                nbElem++;
            }
        }
        if (nbElem == 0) {
            callback.on(result);
        } else {
            long[] keysToLoad = new long[nbElem * 3];
            int lastInsertedIndex = 0;
            for (int i = 0; i < nbKeys; i++) {
                if (toLoadIndexes[i]) {
                    keysToLoad[lastInsertedIndex] = keys[i * KEYS_SIZE];
                    lastInsertedIndex++;
                    keysToLoad[lastInsertedIndex] = keys[i * KEYS_SIZE + 1];
                    lastInsertedIndex++;
                    keysToLoad[lastInsertedIndex] = keys[i * KEYS_SIZE + 2];
                    lastInsertedIndex++;
                }
            }
            _manager.load(keysToLoad, new KCallback<KMemoryElement[]>() {
                @Override
                public void on(KMemoryElement[] loadedElements) {
                    int currentIndexToMerge = 0;
                    for (int i = 0; i < nbKeys; i++) {
                        if (toLoadIndexes[i]) {
                            result[i] = loadedElements[currentIndexToMerge];
                            currentIndexToMerge++;
                        }
                    }
                    callback.on(result);
                }
            });
        }
    }

    public void getRoot(long universe, long time, final KCallback<KObject> callback) {
        final long rootFixedKey = KConfig.END_OF_TIME;
        getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KMemoryElement>() {
            @Override
            public void on(KMemoryElement theGlobalUniverseOrderElement) {
                if (theGlobalUniverseOrderElement == null) {
                    callback.on(null);
                    return;
                }
                getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, rootFixedKey, new KCallback<KMemoryElement>() {
                    @Override
                    public void on(KMemoryElement rootGlobalUniverseOrderElement) {
                        if (rootGlobalUniverseOrderElement == null) {
                            _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                            callback.on(null);
                            return;
                        }
                        long closestUniverse = ResolutionHelper.resolve_universe((KUniverseOrderMap) theGlobalUniverseOrderElement, (KUniverseOrderMap) rootGlobalUniverseOrderElement, time, universe);
                        getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, rootFixedKey, new KCallback<KMemoryElement>() {
                            @Override
                            public void on(KMemoryElement theRootTimeTree) {
                                long resolvedCurrentRootUUID = ((KLongLongTree) theRootTimeTree).previousOrEqualValue(time);
                                _cache.unMarkMemoryElement(theRootTimeTree);
                                _cache.unMarkMemoryElement(rootGlobalUniverseOrderElement);
                                _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                                if (resolvedCurrentRootUUID == KConfig.NULL_LONG) {
                                    callback.on(null);
                                } else {
                                    lookup(universe, time, resolvedCurrentRootUUID, callback);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void setRoot(final KObject newRoot, final KCallback<Throwable> callback) {
        //TODO FIXME

        /*
        final long rootFixedKey = KConfig.END_OF_TIME;
        getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KMemoryElement>() {
            @Override
            public void on(KMemoryElement theGlobalUniverseOrderElement) {
                if (theGlobalUniverseOrderElement == null) {
                    callback.on(null);
                    return;
                }
                getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, rootFixedKey, new KCallback<KMemoryElement>() {
                    @Override
                    public void on(KMemoryElement rootGlobalUniverseOrderElement) {
                        if (rootGlobalUniverseOrderElement == null) {
                            _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                            callback.on(null);
                            return;
                        }
                        long closestUniverse = ResolutionHelper.resolve_universe((KUniverseOrderMap) theGlobalUniverseOrderElement, (KUniverseOrderMap) rootGlobalUniverseOrderElement, time, universe);
                        getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, rootFixedKey, new KCallback<KMemoryElement>() {
                            @Override
                            public void on(KMemoryElement theRootTimeTree) {
                                long resolvedCurrentRootUUID = ((KLongLongTree) theRootTimeTree).previousOrEqualValue(time);
                                _cache.unMarkMemoryElement(theRootTimeTree);
                                _cache.unMarkMemoryElement(rootGlobalUniverseOrderElement);
                                _cache.unMarkMemoryElement(theGlobalUniverseOrderElement);
                                if (resolvedCurrentRootUUID == KConfig.NULL_LONG) {
                                    callback.on(null);
                                } else {
                                    lookup(universe, time, resolvedCurrentRootUUID, callback);
                                }
                            }
                        });
                    }
                });
            }
        });
        */
    }

/*
    @Override
    public void setRoot(final KObject newRoot, final KCallback<Throwable> callback) {


        bumpKeyToCache(KContentKey.createRootUniverseTree(), new KCallback<KMemoryElement>() {
            @Override
            public void on(KMemoryElement globalRootTree) {
                KUniverseOrderMap cleanedTree = (KUniverseOrderMap) globalRootTree;
                if (cleanedTree == null) {
                    cleanedTree = _factory.newUniverseMap(KConfig.CACHE_INIT_SIZE, null);
                    cleanedTree = (KUniverseOrderMap) _cache.getOrPut(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.END_OF_TIME, cleanedTree);
                }
                KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
                long closestUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, cleanedTree, newRoot.now(), newRoot.universe());
                cleanedTree.put(newRoot.universe(), newRoot.now());
                if (closestUniverse != newRoot.universe()) {
                    KLongLongTree newTimeTree = _factory.newLongLongTree();
                    newTimeTree.insert(newRoot.now(), newRoot.uuid());
                    KContentKey universeTreeRootKey = KContentKey.createRootTimeTree(newRoot.universe());
                    _cache.getOrPut(universeTreeRootKey.universe, universeTreeRootKey.time, universeTreeRootKey.obj, (KMemoryElement) newTimeTree);
                    if (callback != null) {
                        callback.on(null);
                    }
                } else {
                    final KContentKey universeTreeRootKey = KContentKey.createRootTimeTree(closestUniverse);
                    bumpKeyToCache(universeTreeRootKey, new KCallback<KMemoryElement>() {
                        @Override
                        public void on(KMemoryElement resolvedRootTimeTree) {
                            KLongLongTree initializedTree = (KLongLongTree) resolvedRootTimeTree;
                            if (initializedTree == null) {
                                initializedTree = _factory.newLongLongTree();
                                initializedTree = (KLongLongTree) _cache.getOrPut(universeTreeRootKey.universe, universeTreeRootKey.time, universeTreeRootKey.obj, (KMemoryElement) initializedTree);
                            }
                            initializedTree.insert(newRoot.now(), newRoot.uuid());
                            if (callback != null) {
                                callback.on(null);
                            }
                        }
                    });
                }
            }
        });
    }
    */
    /* End of root section */


}
