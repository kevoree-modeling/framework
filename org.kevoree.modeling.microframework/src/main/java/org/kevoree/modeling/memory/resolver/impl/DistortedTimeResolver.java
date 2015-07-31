package org.kevoree.modeling.memory.resolver.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.memory.tree.KLongLongTree;
import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.meta.KMetaClass;

public class DistortedTimeResolver implements KResolver {

    private static final int KEYS_SIZE = 3;

    private final KCache _cache;

    private final KInternalDataManager _manager;

    public DistortedTimeResolver(KCache p_cache, KInternalDataManager p_manager) {
        this._cache = p_cache;
        this._manager = p_manager;
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
                                        _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                        callback.on(null);
                                    } else {
                                        long closestUniverse = ResolutionHelper.resolve_universe((KUniverseOrderMap) theGlobalUniverseOrderElement, (KUniverseOrderMap) theObjectUniverseOrderElement, time, universe);
                                        getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, uuid, new KCallback<KMemoryElement>() {
                                            @Override
                                            public void on(KMemoryElement theObjectTimeTreeElement) {
                                                if (theObjectTimeTreeElement == null) {
                                                    _cache.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                    _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                    callback.on(null);
                                                } else {
                                                    long closestTime = ((KLongTree) theObjectTimeTreeElement).previousOrEqual(time);
                                                    getOrLoadAndMark(closestUniverse, closestTime, uuid, new KCallback<KMemoryElement>() {
                                                        @Override
                                                        public void on(KMemoryElement theObjectChunk) {
                                                            if (theObjectChunk == null) {
                                                                _cache.unmarkMemoryElement(theObjectTimeTreeElement);
                                                                _cache.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                                _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                                callback.on(null);
                                                            } else {
                                                                KObject newProxy = ((AbstractKModel) _manager.model()).createProxy(universe, time, uuid, _manager.model().metaModel().metaClass(((KMemoryChunk) theObjectChunk).metaClassIndex()), closestUniverse, closestTime);
                                                                _cache.register(newProxy);
                                                                callback.on(newProxy);
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
        return new Runnable() {
            @Override
            public void run() {
                getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KMemoryElement>() {
                    @Override
                    public void on(KMemoryElement theGlobalUniverseOrderElement) {
                        if (theGlobalUniverseOrderElement != null) {
                            final long[] tempObjectUniverseKeys = new long[uuids.length * 3];
                            for (int i = 0; i < uuids.length; i++) {
                                tempObjectUniverseKeys[i * 3] = KConfig.NULL_LONG;
                                tempObjectUniverseKeys[i * 3 + 1] = KConfig.NULL_LONG;
                                tempObjectUniverseKeys[i * 3 + 2] = uuids[i];
                            }
                            getOrLoadAndMarkAll(tempObjectUniverseKeys, new KCallback<KMemoryElement[]>() {
                                @Override
                                public void on(KMemoryElement[] objectUniverseOrderElements) {
                                    if (objectUniverseOrderElements == null || objectUniverseOrderElements.length == 0) {
                                        _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                        callback.on(null);
                                        return;
                                    }
                                    final long[] tempObjectTimeTreeKeys = new long[uuids.length * 3];
                                    for (int i = 0; i < uuids.length; i++) {
                                        long closestUniverse = ResolutionHelper.resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KUniverseOrderMap) objectUniverseOrderElements[i], time, universe);
                                        tempObjectTimeTreeKeys[i * 3] = closestUniverse;
                                        tempObjectTimeTreeKeys[i * 3 + 1] = KConfig.NULL_LONG;
                                        tempObjectTimeTreeKeys[i * 3 + 2] = uuids[i];
                                    }
                                    getOrLoadAndMarkAll(tempObjectTimeTreeKeys, new KCallback<KMemoryElement[]>() {
                                        @Override
                                        public void on(KMemoryElement[] objectTimeTreeElements) {
                                            if (objectTimeTreeElements == null || objectTimeTreeElements.length == 0) {
                                                _cache.unmarkAllMemoryElements(objectUniverseOrderElements);
                                                _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                callback.on(null);
                                                return;
                                            }
                                            final long[] tempObjectChunkKeys = new long[uuids.length * 3];
                                            for (int i = 0; i < uuids.length; i++) {
                                                long closestTime = ((KLongTree) objectTimeTreeElements[i]).previousOrEqual(time);
                                                tempObjectChunkKeys[i * 3] = tempObjectTimeTreeKeys[i * 3];
                                                tempObjectChunkKeys[i * 3 + 1] = closestTime;
                                                tempObjectChunkKeys[i * 3 + 2] = uuids[i];
                                            }
                                            getOrLoadAndMarkAll(tempObjectChunkKeys, new KCallback<KMemoryElement[]>() {
                                                @Override
                                                public void on(KMemoryElement[] theObjectChunks) {
                                                    if (theObjectChunks == null || theObjectChunks.length == 0) {
                                                        _cache.unmarkAllMemoryElements(objectTimeTreeElements);
                                                        _cache.unmarkAllMemoryElements(objectUniverseOrderElements);
                                                        _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                        callback.on(null);
                                                    } else {
                                                        KObject[] finalResult = new KObject[uuids.length];
                                                        for (int h = 0; h < theObjectChunks.length; h++) {
                                                            finalResult[h] = ((AbstractKModel) _manager.model()).createProxy(universe, time, uuids[h], _manager.model().metaModel().metaClass(((KMemoryChunk) theObjectChunks[h]).metaClassIndex()), tempObjectTimeTreeKeys[h * 3], tempObjectChunkKeys[h * 3 + 1]);
                                                        }
                                                        _cache.registerAll(finalResult);
                                                        callback.on(finalResult);
                                                    }
                                                }
                                            });

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        };
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
        KMemoryChunk currentEntry;
        if (previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] == universe && previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] == time) {
            currentEntry = (KMemoryChunk) _cache.unsafeGet(universe, time, uuid);
            if (currentEntry != null) {
                return currentEntry;
            }
        } else {
            currentEntry = (KMemoryChunk) _cache.getAndMark(universe, time, uuid);
            if (currentEntry != null) {
                _cache.unmark(previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid);
                previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = universe;
                previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = time;
                return currentEntry;
            }
        }
        KUniverseOrderMap objectUniverseTree = (KUniverseOrderMap) _cache.getAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid);
        if (objectUniverseTree == null) {
            return null;
        }
        KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _cache.getAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (globalUniverseTree == null) {
            _cache.unmarkMemoryElement(objectUniverseTree);
            return null;
        }
        long resolvedUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, objectUniverseTree, time, universe);
        KLongTree timeTree = (KLongTree) _cache.getAndMark(resolvedUniverse, KConfig.NULL_LONG, uuid);
        if (timeTree == null) {
            _cache.unmarkMemoryElement(globalUniverseTree);
            _cache.unmarkMemoryElement(objectUniverseTree);
            return null;
        }
        long resolvedTime = timeTree.previousOrEqual(time);
        if (resolvedTime != KConfig.NULL_LONG) {
            boolean needTimeCopy = !useClosest && (resolvedTime != time);
            boolean needUniverseCopy = !useClosest && (resolvedUniverse != universe);
            boolean wasPreviouslyTheSameUniverseTime = previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] == resolvedUniverse && previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] == resolvedTime;
            if (wasPreviouslyTheSameUniverseTime) {
                currentEntry = (KMemoryChunk) _cache.unsafeGet(resolvedUniverse, resolvedTime, uuid);
            } else {
                currentEntry = (KMemoryChunk) _cache.getAndMark(resolvedUniverse, resolvedTime, uuid);
            }
            if (currentEntry == null) {
                if (!wasPreviouslyTheSameUniverseTime) {
                    _cache.unmark(previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid);
                    previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = resolvedUniverse;
                    previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = resolvedTime;
                }
                _cache.unmarkMemoryElement(timeTree);
                _cache.unmarkMemoryElement(globalUniverseTree);
                _cache.unmarkMemoryElement(objectUniverseTree);
                return null;
            }
            if (!needTimeCopy && !needUniverseCopy) {
                if (!wasPreviouslyTheSameUniverseTime) {
                    _cache.unmark(previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid);
                    previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = resolvedUniverse;
                    previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = resolvedTime;
                } else {
                    _cache.unmarkMemoryElement(currentEntry);
                }
                _cache.unmarkMemoryElement(timeTree);
                _cache.unmarkMemoryElement(globalUniverseTree);
                _cache.unmarkMemoryElement(objectUniverseTree);
                return currentEntry;
            } else {
                KMemoryChunk clonedChunk = _cache.cloneMarkAndUnmark(currentEntry, universe, time, uuid, _manager.model().metaModel());
                if (!needUniverseCopy) {
                    timeTree.insert(time);
                } else {
                    KLongTree newTemporalTree = (KLongTree) _cache.createAndMark(resolvedUniverse, KConfig.NULL_LONG, uuid, KMemoryElementTypes.LONG_TREE);
                    newTemporalTree.insert(resolvedTime);
                    _cache.unmarkMemoryElement(timeTree);
                    objectUniverseTree.put(resolvedUniverse, resolvedTime);
                }
                previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = resolvedUniverse;
                previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = resolvedTime;
                _cache.unmarkMemoryElement(timeTree);
                _cache.unmarkMemoryElement(globalUniverseTree);
                _cache.unmarkMemoryElement(objectUniverseTree);
                return clonedChunk;
            }
        } else {
            _cache.unmarkMemoryElement(timeTree);
            _cache.unmarkMemoryElement(globalUniverseTree);
            _cache.unmarkMemoryElement(objectUniverseTree);
            return null;
        }
    }

    @Override
    public void indexObject(KObject obj) {
        int metaClassIndex = obj.metaClass().index();
        KMemoryChunk cacheEntry = (KMemoryChunk) _cache.createAndMark(obj.universe(), obj.now(), obj.uuid(), KMemoryElementTypes.CHUNK);
        cacheEntry.init(null, _manager.model().metaModel(), metaClassIndex);
        cacheEntry.setDirty();
        //initiate time management
        KLongTree timeTree = (KLongTree) _cache.createAndMark(obj.universe(), KConfig.NULL_LONG, obj.uuid(), KMemoryElementTypes.LONG_TREE);
        timeTree.init(null, _manager.model().metaModel(), metaClassIndex);
        timeTree.insert(obj.now());
        //initiate universe management
        KUniverseOrderMap universeTree = (KUniverseOrderMap) _cache.createAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, obj.uuid(), KMemoryElementTypes.LONG_LONG_MAP);
        universeTree.init(null, _manager.model().metaModel(), metaClassIndex);
        universeTree.put(obj.universe(), obj.now());
        _cache.register(obj);
    }

    @Override
    public short typeFromKey(long universe, long time, long uuid) {
        boolean isUniverseNotNull = universe != KConfig.NULL_LONG;
        short result = -1;
        if (KConfig.END_OF_TIME == uuid) {
            if (isUniverseNotNull) {
                result = KMemoryElementTypes.LONG_LONG_TREE;
            } else {
                result = KMemoryElementTypes.LONG_LONG_MAP;
            }
        } else {
            boolean isTimeNotNull = time != KConfig.NULL_LONG;
            boolean isObjNotNull = uuid != KConfig.NULL_LONG;
            if (isUniverseNotNull && isTimeNotNull && isObjNotNull) {
                result = KMemoryElementTypes.CHUNK;
            } else if (isUniverseNotNull && !isTimeNotNull && isObjNotNull) {
                result = KMemoryElementTypes.LONG_TREE;
            } else {
                result = KMemoryElementTypes.LONG_LONG_MAP;
            }
        }
        return result;
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
                            _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
                            callback.on(null);
                            return;
                        }
                        long closestUniverse = ResolutionHelper.resolve_universe((KUniverseOrderMap) theGlobalUniverseOrderElement, (KUniverseOrderMap) rootGlobalUniverseOrderElement, time, universe);
                        getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, rootFixedKey, new KCallback<KMemoryElement>() {
                            @Override
                            public void on(KMemoryElement theRootTimeTree) {
                                long resolvedCurrentRootUUID = ((KLongLongTree) theRootTimeTree).previousOrEqualValue(time);
                                _cache.unmarkMemoryElement(theRootTimeTree);
                                _cache.unmarkMemoryElement(rootGlobalUniverseOrderElement);
                                _cache.unmarkMemoryElement(theGlobalUniverseOrderElement);
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
