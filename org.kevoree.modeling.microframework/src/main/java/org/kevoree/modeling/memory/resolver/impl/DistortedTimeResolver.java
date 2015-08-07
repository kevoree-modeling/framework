package org.kevoree.modeling.memory.resolver.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.chunk.KLongLongTree;
import org.kevoree.modeling.memory.chunk.KLongTree;
import org.kevoree.modeling.memory.chunk.KTreeWalker;
import org.kevoree.modeling.meta.KMetaClass;

public class DistortedTimeResolver implements KResolver {

    private static final int KEYS_SIZE = 3;

    private final KChunkSpaceManager _spaceManager;

    private final KInternalDataManager _manager;

    public DistortedTimeResolver(KChunkSpaceManager p_cache, KInternalDataManager p_manager) {
        this._spaceManager = p_cache;
        this._manager = p_manager;
        this._spaceManager.setResolver(this);
    }

    @Override
    public final Runnable lookup(final long universe, final long time, final long uuid, final KCallback<KObject> callback) {
        final DistortedTimeResolver selfPointer = this;
        return new Runnable() {
            @Override
            public void run() {
                selfPointer.getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KChunk>() {
                    @Override
                    public void on(KChunk theGlobalUniverseOrderElement) {
                        if (theGlobalUniverseOrderElement != null) {
                            selfPointer.getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid, new KCallback<KChunk>() {
                                @Override
                                public void on(KChunk theObjectUniverseOrderElement) {
                                    if (theObjectUniverseOrderElement == null) {
                                        selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                        callback.on(null);
                                    } else {
                                        long closestUniverse = resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) theObjectUniverseOrderElement, time, universe);
                                        selfPointer.getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, uuid, new KCallback<KChunk>() {
                                            @Override
                                            public void on(KChunk theObjectTimeTreeElement) {
                                                if (theObjectTimeTreeElement == null) {
                                                    selfPointer._spaceManager.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                    selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                    callback.on(null);
                                                } else {
                                                    long closestTime = ((KLongTree) theObjectTimeTreeElement).previousOrEqual(time);
                                                    if (closestTime == KConfig.NULL_LONG) {
                                                        selfPointer._spaceManager.unmarkMemoryElement(theObjectTimeTreeElement);
                                                        selfPointer._spaceManager.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                        selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                        callback.on(null);
                                                        return;
                                                    }
                                                    selfPointer.getOrLoadAndMark(closestUniverse, closestTime, uuid, new KCallback<KChunk>() {
                                                        @Override
                                                        public void on(KChunk theObjectChunk) {
                                                            if (theObjectChunk == null) {
                                                                selfPointer._spaceManager.unmarkMemoryElement(theObjectTimeTreeElement);
                                                                selfPointer._spaceManager.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                                selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                                callback.on(null);
                                                            } else {
                                                                KObject newProxy = ((AbstractKModel) selfPointer._manager.model()).createProxy(universe, time, uuid, selfPointer._manager.model().metaModel().metaClass(((KObjectChunk) theObjectChunk).metaClassIndex()), closestUniverse, closestTime);
                                                                selfPointer._spaceManager.register(newProxy);
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

    @Override
    public final Runnable lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback) {
        final DistortedTimeResolver selfPointer = this;
        return new Runnable() {
            @Override
            public void run() {
                selfPointer.getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KChunk>() {
                    @Override
                    public void on(KChunk theGlobalUniverseOrderElement) {
                        if (theGlobalUniverseOrderElement != null) {
                            final long[] tempObjectUniverseKeys = new long[uuids.length * 3];
                            for (int i = 0; i < uuids.length; i++) {
                                tempObjectUniverseKeys[i * 3] = KConfig.NULL_LONG;
                                tempObjectUniverseKeys[i * 3 + 1] = KConfig.NULL_LONG;
                                tempObjectUniverseKeys[i * 3 + 2] = uuids[i];
                            }
                            selfPointer.getOrLoadAndMarkAll(tempObjectUniverseKeys, new KCallback<KChunk[]>() {
                                @Override
                                public void on(KChunk[] objectUniverseOrderElements) {
                                    if (objectUniverseOrderElements == null || objectUniverseOrderElements.length == 0) {
                                        selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                        callback.on(new KObject[0]);
                                        return;
                                    }
                                    final long[] tempObjectTimeTreeKeys = new long[uuids.length * 3];
                                    for (int i = 0; i < uuids.length; i++) {
                                        long closestUniverse = resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) objectUniverseOrderElements[i], time, universe);
                                        tempObjectTimeTreeKeys[i * 3] = closestUniverse;
                                        tempObjectTimeTreeKeys[i * 3 + 1] = KConfig.NULL_LONG;
                                        tempObjectTimeTreeKeys[i * 3 + 2] = uuids[i];
                                    }
                                    selfPointer.getOrLoadAndMarkAll(tempObjectTimeTreeKeys, new KCallback<KChunk[]>() {
                                        @Override
                                        public void on(KChunk[] objectTimeTreeElements) {
                                            if (objectTimeTreeElements == null || objectTimeTreeElements.length == 0) {
                                                selfPointer._spaceManager.unmarkAllMemoryElements(objectUniverseOrderElements);
                                                selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                callback.on(new KObject[0]);
                                                return;
                                            }
                                            final long[] tempObjectChunkKeys = new long[uuids.length * 3];
                                            for (int i = 0; i < uuids.length; i++) {
                                                long closestTime = ((KLongTree) objectTimeTreeElements[i]).previousOrEqual(time);
                                                if (closestTime != KConfig.NULL_LONG) {
                                                    tempObjectChunkKeys[i * 3] = tempObjectTimeTreeKeys[i * 3];
                                                    tempObjectChunkKeys[i * 3 + 1] = closestTime;
                                                    tempObjectChunkKeys[i * 3 + 2] = uuids[i];
                                                } else {
                                                    System.arraycopy(KContentKey.NULL_KEY, 0, tempObjectChunkKeys, (i * 3), 3);
                                                }
                                            }
                                            selfPointer.getOrLoadAndMarkAll(tempObjectChunkKeys, new KCallback<KChunk[]>() {
                                                @Override
                                                public void on(KChunk[] theObjectChunks) {
                                                    if (theObjectChunks == null || theObjectChunks.length == 0) {
                                                        selfPointer._spaceManager.unmarkAllMemoryElements(objectTimeTreeElements);
                                                        selfPointer._spaceManager.unmarkAllMemoryElements(objectUniverseOrderElements);
                                                        selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                        callback.on(new KObject[0]);
                                                    } else {
                                                        KObject[] finalResult = new KObject[uuids.length];
                                                        for (int h = 0; h < theObjectChunks.length; h++) {
                                                            if (theObjectChunks[h] != null) {
                                                                finalResult[h] = ((AbstractKModel) selfPointer._manager.model()).createProxy(universe, time, uuids[h], selfPointer._manager.model().metaModel().metaClass(((KObjectChunk) theObjectChunks[h]).metaClassIndex()), tempObjectTimeTreeKeys[h * 3], tempObjectChunkKeys[h * 3 + 1]);
                                                            } else {
                                                                finalResult[h] = null;
                                                            }
                                                        }
                                                        selfPointer._spaceManager.registerAll(finalResult);
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
    public final Runnable lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback) {
        final DistortedTimeResolver selfPointer = this;
        return new Runnable() {
            @Override
            public void run() {
                selfPointer.getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KChunk>() {
                    @Override
                    public void on(KChunk theGlobalUniverseOrderElement) {
                        if (theGlobalUniverseOrderElement != null) {
                            selfPointer.getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid, new KCallback<KChunk>() {
                                @Override
                                public void on(KChunk theObjectUniverseOrderElement) {
                                    if (theObjectUniverseOrderElement == null) {
                                        selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                        callback.on(null);
                                    } else {
                                        final long[] closestUniverses = new long[times.length];
                                        ArrayLongLongMap closestUnikUniverse = new ArrayLongLongMap(-1, -1, -1, null);
                                        int nbUniverseToload = 0;
                                        for (int i = 0; i < times.length; i++) {
                                            closestUniverses[i] = resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) theObjectUniverseOrderElement, times[i], universe);
                                            if (!closestUnikUniverse.contains(closestUniverses[i])) {
                                                closestUnikUniverse.put(closestUniverses[i], nbUniverseToload);
                                                nbUniverseToload++;
                                            }
                                        }
                                        long[] toLoadUniverseKeys = new long[nbUniverseToload * 3];
                                        closestUnikUniverse.each(new KLongLongMapCallBack() {
                                            @Override
                                            public void on(long key, long value) {
                                                int currentIndex = (int) (value * 3);
                                                toLoadUniverseKeys[currentIndex] = value;
                                                toLoadUniverseKeys[currentIndex + 1] = KConfig.NULL_LONG;
                                                toLoadUniverseKeys[currentIndex + 2] = uuid;
                                            }
                                        });
                                        selfPointer.getOrLoadAndMarkAll(toLoadUniverseKeys, new KCallback<KChunk[]>() {
                                            @Override
                                            public void on(KChunk[] objectTimeTreeElements) {
                                                if (objectTimeTreeElements == null || objectTimeTreeElements.length == 0) {
                                                    selfPointer._spaceManager.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                    selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                    callback.on(null);
                                                } else {
                                                    final long[] closestTimes = new long[times.length];
                                                    final ArrayLongLongMap closestUnikTimes = new ArrayLongLongMap(-1, -1, -1, null);
                                                    ArrayLongLongMap reverseTimeUniverse = new ArrayLongLongMap(-1, -1, -1, null);
                                                    int nbTimesToload = 0;
                                                    for (int i = 0; i < times.length; i++) {
                                                        int alignedIndexOfUniverse = (int) closestUnikUniverse.get(closestUniverses[i]);
                                                        closestTimes[i] = ((KLongTree) objectTimeTreeElements[alignedIndexOfUniverse]).previousOrEqual(times[i]);
                                                        if (!closestUnikTimes.contains(closestTimes[i])) {
                                                            closestUnikTimes.put(closestTimes[i], nbTimesToload);
                                                            reverseTimeUniverse.put(closestTimes[i], closestUniverses[i]);
                                                            nbTimesToload++;
                                                        }
                                                    }
                                                    long[] toLoadTimesKeys = new long[nbTimesToload * 3];
                                                    closestUnikTimes.each(new KLongLongMapCallBack() {
                                                        @Override
                                                        public void on(long key, long value) {
                                                            int currentIndex = (int) (value * 3);
                                                            toLoadTimesKeys[currentIndex] = reverseTimeUniverse.get(key);
                                                            toLoadTimesKeys[currentIndex + 1] = key;
                                                            toLoadTimesKeys[currentIndex + 2] = uuid;
                                                        }
                                                    });
                                                    selfPointer.getOrLoadAndMarkAll(toLoadTimesKeys, new KCallback<KChunk[]>() {
                                                        @Override
                                                        public void on(KChunk[] objectChunks) {
                                                            if (objectChunks == null || objectChunks.length == 0) {
                                                                selfPointer._spaceManager.unmarkAllMemoryElements(objectTimeTreeElements);
                                                                selfPointer._spaceManager.unmarkMemoryElement(theObjectUniverseOrderElement);
                                                                selfPointer._spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                                                callback.on(null);
                                                            } else {
                                                                KObject[] result = new KObject[times.length];
                                                                for (int i = 0; i < times.length; i++) {
                                                                    long resolvedUniverse = closestUniverses[i];
                                                                    long resolvedTime = closestTimes[i];
                                                                    int indexChunks = (int) closestUnikTimes.get(closestTimes[i]);
                                                                    if (indexChunks != -1 && resolvedUniverse != KConfig.NULL_LONG && resolvedTime != KConfig.NULL_LONG) {
                                                                        result[i] = ((AbstractKModel) selfPointer._manager.model()).createProxy(universe, times[i], uuid, selfPointer._manager.model().metaModel().metaClass(((KObjectChunk) objectChunks[indexChunks]).metaClassIndex()), resolvedUniverse, resolvedTime);
                                                                    } else {
                                                                        result[i] = null;
                                                                    }
                                                                }
                                                                selfPointer._spaceManager.registerAll(result);
                                                                callback.on(result);
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

    @Override
    public KObjectChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        return internal_chunk(universe, time, uuid, false, metaClass, previousResolution);
    }

    @Override
    public KObjectChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        return internal_chunk(universe, time, uuid, true, metaClass, previousResolution);
    }

    //TODO optimize the worst case by reusing, by using previous universe cache information, maybe optimize
    //FIXME
    private KObjectChunk internal_chunk(long universe, long requestedTime, long uuid, boolean useClosest, KMetaClass metaClass, long[] previousResolution) {
        long time = requestedTime;
        if (metaClass.temporalResolution() != 1) {
            time = time - (time % metaClass.temporalResolution());
        }
        KObjectChunk currentEntry;
        if (previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] == universe && previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] == time) {
            currentEntry = (KObjectChunk) _spaceManager.unsafeGet(universe, time, uuid);
            if (currentEntry != null) {
                return currentEntry;
            }
        } else {
            currentEntry = (KObjectChunk) _spaceManager.getAndMark(universe, time, uuid);
            if (currentEntry != null) {
                _spaceManager.unmark(previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid);
                previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = universe;
                previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = time;
                return currentEntry;
            }
        }
        KLongLongMap objectUniverseTree = (KLongLongMap) _spaceManager.getAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid);
        if (objectUniverseTree == null) {
            return null;
        }
        KLongLongMap globalUniverseTree = (KLongLongMap) _spaceManager.getAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (globalUniverseTree == null) {
            _spaceManager.unmarkMemoryElement(objectUniverseTree);
            return null;
        }
        long resolvedUniverse = resolve_universe(globalUniverseTree, objectUniverseTree, time, universe);
        KLongTree timeTree = (KLongTree) _spaceManager.getAndMark(resolvedUniverse, KConfig.NULL_LONG, uuid);
        if (timeTree == null) {
            _spaceManager.unmarkMemoryElement(globalUniverseTree);
            _spaceManager.unmarkMemoryElement(objectUniverseTree);
            return null;
        }
        long resolvedTime = timeTree.previousOrEqual(time);
        if (resolvedTime != KConfig.NULL_LONG) {
            boolean needTimeCopy = !useClosest && (resolvedTime != time);
            boolean needUniverseCopy = !useClosest && (resolvedUniverse != universe);
            boolean wasPreviouslyTheSameUniverseTime = previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] == resolvedUniverse && previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] == resolvedTime;
            if (wasPreviouslyTheSameUniverseTime) {
                currentEntry = (KObjectChunk) _spaceManager.unsafeGet(resolvedUniverse, resolvedTime, uuid);
            } else {
                currentEntry = (KObjectChunk) _spaceManager.getAndMark(resolvedUniverse, resolvedTime, uuid);
            }
            if (currentEntry == null) {
                if (!wasPreviouslyTheSameUniverseTime) {
                    _spaceManager.unmark(previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid);
                    previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = resolvedUniverse;
                    previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = resolvedTime;
                }
                _spaceManager.unmarkMemoryElement(timeTree);
                _spaceManager.unmarkMemoryElement(globalUniverseTree);
                _spaceManager.unmarkMemoryElement(objectUniverseTree);
                return null;
            }
            if (!needTimeCopy && !needUniverseCopy) {
                if (!wasPreviouslyTheSameUniverseTime) {
                    _spaceManager.unmark(previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid);
                    previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = resolvedUniverse;
                    previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = resolvedTime;
                } else {
                    _spaceManager.unmarkMemoryElement(currentEntry);
                }
                _spaceManager.unmarkMemoryElement(timeTree);
                _spaceManager.unmarkMemoryElement(globalUniverseTree);
                _spaceManager.unmarkMemoryElement(objectUniverseTree);
                return currentEntry;
            } else {
                KObjectChunk clonedChunk = _spaceManager.cloneMarkAndUnmark(currentEntry, universe, time, uuid, _manager.model().metaModel());
                if (!needUniverseCopy) {
                    timeTree.insert(time);
                } else {
                    KLongTree newTemporalTree = (KLongTree) _spaceManager.createAndMark(universe, KConfig.NULL_LONG, uuid, KChunkTypes.LONG_TREE);
                    newTemporalTree.insert(time);
                    _spaceManager.unmarkMemoryElement(timeTree);
                    objectUniverseTree.put(universe, time);
                }
                previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX] = universe;
                previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX] = time;
                _spaceManager.unmarkMemoryElement(timeTree);
                _spaceManager.unmarkMemoryElement(globalUniverseTree);
                _spaceManager.unmarkMemoryElement(objectUniverseTree);
                return clonedChunk;
            }
        } else {
            _spaceManager.unmarkMemoryElement(timeTree);
            _spaceManager.unmarkMemoryElement(globalUniverseTree);
            _spaceManager.unmarkMemoryElement(objectUniverseTree);
            return null;
        }
    }

    @Override
    public void indexObject(KObject obj) {
        int metaClassIndex = obj.metaClass().index();
        KObjectChunk cacheEntry = (KObjectChunk) _spaceManager.createAndMark(obj.universe(), obj.now(), obj.uuid(), KChunkTypes.OBJECT_CHUNK);
        cacheEntry.init(null, _manager.model().metaModel(), metaClassIndex);
        cacheEntry.setFlags(KChunkFlags.DIRTY_BIT, 0);
        cacheEntry.space().declareDirty(cacheEntry);

        //initiate time management
        KLongTree timeTree = (KLongTree) _spaceManager.createAndMark(obj.universe(), KConfig.NULL_LONG, obj.uuid(), KChunkTypes.LONG_TREE);
        timeTree.init(null, _manager.model().metaModel(), metaClassIndex);
        timeTree.insert(obj.now());
        //initiate universe management
        KLongLongMap universeTree = (KLongLongMap) _spaceManager.createAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, obj.uuid(), KChunkTypes.LONG_LONG_MAP);
        universeTree.init(null, _manager.model().metaModel(), metaClassIndex);
        universeTree.put(obj.universe(), obj.now());
        _spaceManager.register(obj);
    }

    @Override
    public short typeFromKey(long universe, long time, long uuid) {
        boolean isUniverseNotNull = universe != KConfig.NULL_LONG;
        short result;
        if (KConfig.END_OF_TIME == uuid) {
            if (isUniverseNotNull) {
                result = KChunkTypes.LONG_LONG_TREE;
            } else {
                result = KChunkTypes.LONG_LONG_MAP;
            }
        } else {
            boolean isTimeNotNull = time != KConfig.NULL_LONG;
            boolean isObjNotNull = uuid != KConfig.NULL_LONG;
            if (isUniverseNotNull && isTimeNotNull && isObjNotNull) {
                result = KChunkTypes.OBJECT_CHUNK;
            } else if (isUniverseNotNull && !isTimeNotNull && isObjNotNull) {
                result = KChunkTypes.LONG_TREE;
            } else {
                result = KChunkTypes.LONG_LONG_MAP;
            }
        }
        return result;
    }

    public final void getOrLoadAndMark(long universe, long time, long uuid, final KCallback<KChunk> callback) {
        if (universe == KContentKey.NULL_KEY[0] && time == KContentKey.NULL_KEY[1] && uuid == KContentKey.NULL_KEY[2]) {
            callback.on(null);
            return;
        }
        KChunk cached = _spaceManager.getAndMark(universe, time, uuid);
        if (cached != null) {
            callback.on(cached);
        } else {
            load(new long[]{universe, time, uuid}, new KCallback<KChunk[]>() {
                @Override
                public void on(KChunk[] loadedElements) {
                    callback.on(loadedElements[0]);
                }
            });
        }
    }

    public final void getOrLoadAndMarkAll(long[] keys, final KCallback<KChunk[]> callback) {
        int nbKeys = keys.length / KEYS_SIZE;
        final boolean[] toLoadIndexes = new boolean[nbKeys];
        int nbElem = 0;
        final KChunk[] result = new KChunk[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            if (keys[i * KEYS_SIZE] == KContentKey.NULL_KEY[0] && keys[i * KEYS_SIZE + 1] == KContentKey.NULL_KEY[1] && keys[i * KEYS_SIZE + 2] == KContentKey.NULL_KEY[2]) {
                toLoadIndexes[i] = false;
                result[i] = null;
            } else {
                result[i] = _spaceManager.getAndMark(keys[i * KEYS_SIZE], keys[i * KEYS_SIZE + 1], keys[i * KEYS_SIZE + 2]);
                if (result[i] == null) {
                    toLoadIndexes[i] = true;
                    nbElem++;
                } else {
                    toLoadIndexes[i] = false;
                }
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
            load(keysToLoad, new KCallback<KChunk[]>() {
                @Override
                public void on(KChunk[] loadedElements) {
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

    //TODO, ROOT TREE is NEVER UNLOAD
    public void getRoot(long universe, long time, final KCallback<KObject> callback) {
        final long rootFixedKey = KConfig.END_OF_TIME;
        getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KChunk>() {
            @Override
            public void on(KChunk theGlobalUniverseOrderElement) {
                if (theGlobalUniverseOrderElement == null) {
                    callback.on(null);
                    return;
                }
                getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, rootFixedKey, new KCallback<KChunk>() {
                    @Override
                    public void on(KChunk rootGlobalUniverseOrderElement) {
                        if (rootGlobalUniverseOrderElement == null) {
                            _spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                            callback.on(null);
                            return;
                        }
                        long closestUniverse = resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) rootGlobalUniverseOrderElement, time, universe);
                        getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, rootFixedKey, new KCallback<KChunk>() {
                            @Override
                            public void on(KChunk theRootTimeTree) {
                                long resolvedCurrentRootUUID = ((KLongLongTree) theRootTimeTree).previousOrEqualValue(time);
                                _spaceManager.unmarkMemoryElement(theRootTimeTree);
                                _spaceManager.unmarkMemoryElement(rootGlobalUniverseOrderElement);
                                _spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                if (resolvedCurrentRootUUID == KConfig.NULL_LONG) {
                                    callback.on(null);
                                } else {
                                    _manager.lookup(universe, time, resolvedCurrentRootUUID, callback);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void setRoot(final KObject newRoot, final KCallback<Throwable> callback) {
        final long rootFixedKey = KConfig.END_OF_TIME;
        getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, new KCallback<KChunk>() {
            @Override
            public void on(KChunk theGlobalUniverseOrderElement) {
                if (theGlobalUniverseOrderElement == null) {
                    callback.on(null);
                    return;
                }
                getOrLoadAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, rootFixedKey, new KCallback<KChunk>() {
                    @Override
                    public void on(KChunk rootGlobalUniverseOrderElement) {
                        KLongLongMap rootGlobalUniverseOrder = (KLongLongMap) rootGlobalUniverseOrderElement;
                        if (rootGlobalUniverseOrderElement == null) {
                            rootGlobalUniverseOrder = (KLongLongMap) _spaceManager.createAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.END_OF_TIME, KChunkTypes.LONG_LONG_MAP);
                        }
                        long closestUniverse = resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) rootGlobalUniverseOrderElement, newRoot.now(), newRoot.universe());
                        rootGlobalUniverseOrder.put(newRoot.universe(), newRoot.now());
                        if (closestUniverse != newRoot.universe()) {
                            KLongLongTree newTimeTree = (KLongLongTree) _spaceManager.createAndMark(newRoot.universe(), KConfig.NULL_LONG, KConfig.END_OF_TIME, KChunkTypes.LONG_LONG_TREE);
                            newTimeTree.insert(newRoot.now(), newRoot.uuid());
                            _spaceManager.unmarkMemoryElement(newTimeTree);
                            _spaceManager.unmarkMemoryElement(rootGlobalUniverseOrderElement);
                            _spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                            if (callback != null) {
                                callback.on(null);
                            }
                        } else {
                            getOrLoadAndMark(closestUniverse, KConfig.NULL_LONG, KConfig.END_OF_TIME, new KCallback<KChunk>() {
                                @Override
                                public void on(KChunk resolvedRootTimeTree) {
                                    KLongLongTree initializedTree = (KLongLongTree) resolvedRootTimeTree;
                                    if (initializedTree == null) {
                                        initializedTree = (KLongLongTree) _spaceManager.createAndMark(closestUniverse, KConfig.NULL_LONG, KConfig.END_OF_TIME, KChunkTypes.LONG_LONG_TREE);
                                    }
                                    initializedTree.insert(newRoot.now(), newRoot.uuid());
                                    _spaceManager.unmarkMemoryElement(resolvedRootTimeTree);
                                    _spaceManager.unmarkMemoryElement(rootGlobalUniverseOrderElement);
                                    _spaceManager.unmarkMemoryElement(theGlobalUniverseOrderElement);
                                    if (callback != null) {
                                        callback.on(null);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }


    @Override
    public void resolveTimes(final long currentUniverse, final long currentUuid, final long startTime, final long endTime, KCallback<long[]> callback) {
        long[] keys = new long[]{
                KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG,
                KConfig.NULL_LONG, KConfig.NULL_LONG, currentUuid
        };
        getOrLoadAndMarkAll(keys, new KCallback<KChunk[]>() {
            @Override
            public void on(KChunk[] kMemoryChunks) {
                if (kMemoryChunks == null || kMemoryChunks.length == 0) {
                    callback.on(new long[0]);
                    return;
                }
                final long[] collectedUniverse = universeSelectByRange((KLongLongMap) kMemoryChunks[0], (KLongLongMap) kMemoryChunks[1], startTime, endTime, currentUniverse);
                int nbKeys = collectedUniverse.length * 3;
                final long[] timeTreeKeys = new long[nbKeys];
                for (int i = 0; i < collectedUniverse.length; i++) {
                    timeTreeKeys[i * 3] = collectedUniverse[i];
                    timeTreeKeys[i * 3 + 1] = KConfig.NULL_LONG;
                    timeTreeKeys[i * 3 + 2] = currentUuid;
                }
                final KLongLongMap objUniverse = (KLongLongMap) kMemoryChunks[1];
                getOrLoadAndMarkAll(timeTreeKeys, new KCallback<KChunk[]>() {
                    @Override
                    public void on(KChunk[] timeTrees) {
                        if (timeTrees == null || timeTrees.length == 0) {
                            _spaceManager.unmarkAllMemoryElements(kMemoryChunks);
                            callback.on(new long[0]);
                            return;
                        }
                        ArrayLongLongMap collector = new ArrayLongLongMap(-1, -1, -1, null);
                        long previousDivergenceTime = endTime;
                        for (int i = 0; i < collectedUniverse.length; i++) {
                            KLongTree timeTree = (KLongTree) timeTrees[i];
                            if (timeTree != null) {
                                long currentDivergenceTime = objUniverse.get(collectedUniverse[i]);
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
                        _spaceManager.unmarkAllMemoryElements(timeTrees);
                        _spaceManager.unmarkAllMemoryElements(kMemoryChunks);
                        callback.on(orderedTime);
                    }
                });
            }
        });
    }

    public final static long resolve_universe(KLongLongMap globalTree, KLongLongMap objUniverseTree, long timeToResolve, long originUniverseId) {
        if (globalTree == null || objUniverseTree == null) {
            return originUniverseId;
        }
        long currentUniverse = originUniverseId;
        long previousUniverse = KConfig.NULL_LONG;
        long divergenceTime = objUniverseTree.get(currentUniverse);
        while (currentUniverse != previousUniverse) {
            //check range
            if (divergenceTime != KConfig.NULL_LONG && divergenceTime <= timeToResolve) {
                return currentUniverse;
            }
            //next round
            previousUniverse = currentUniverse;
            currentUniverse = globalTree.get(currentUniverse);
            divergenceTime = objUniverseTree.get(currentUniverse);
        }
        return originUniverseId;
    }

    public final static long[] universeSelectByRange(KLongLongMap globalTree, KLongLongMap objUniverseTree, long rangeMin, long rangeMax, long originUniverseId) {
        KLongLongMap collected = new ArrayLongLongMap(-1, -1, -1, null);
        long currentUniverse = originUniverseId;
        long previousUniverse = KConfig.NULL_LONG;
        long divergenceTime = objUniverseTree.get(currentUniverse);
        while (currentUniverse != previousUniverse) {
            //check range
            if (divergenceTime != KConfig.NULL_LONG) {
                if (divergenceTime <= rangeMin) {
                    collected.put(collected.size(), currentUniverse);
                    break;
                } else if (divergenceTime <= rangeMax) {
                    collected.put(collected.size(), currentUniverse);
                }
            }
            //next round
            previousUniverse = currentUniverse;
            currentUniverse = globalTree.get(currentUniverse);
            divergenceTime = objUniverseTree.get(currentUniverse);
        }
        long[] trimmed = new long[collected.size()];
        for (long i = 0; i < collected.size(); i++) {
            trimmed[(int) i] = collected.get(i);
        }
        return trimmed;
    }

    private void load(long[] keys, KCallback<KChunk[]> callback) {
        this._manager.cdn().get(keys, new KCallback<String[]>() {
            @Override
            public void on(String[] payloads) {
                KChunk[] results = new KChunk[keys.length / 3];
                for (int i = 0; i < payloads.length; i++) {
                    long loopUniverse = keys[i * 3];
                    long loopTime = keys[i * 3 + 1];
                    long loopUuid = keys[i * 3 + 2];
                    results[i] = _spaceManager.createAndMark(loopUniverse, loopTime, loopUuid, typeFromKey(loopUniverse, loopTime, loopUuid));
                    int classIndex = -1;
                    if (loopUniverse != KConfig.NULL_LONG && loopTime != KConfig.NULL_LONG && loopUuid != KConfig.NULL_LONG) {
                        KLongLongMap alreadyLoadedOrder = (KLongLongMap) _spaceManager.unsafeGet(KConfig.NULL_LONG, KConfig.NULL_LONG, loopUuid);
                        if (alreadyLoadedOrder != null) {
                            classIndex = alreadyLoadedOrder.metaClassIndex();
                        }
                    }
                    results[i].init(payloads[i], _manager.model().metaModel(), classIndex);
                }
                callback.on(results);
            }
        });
    }

    @Override
    public long[] getRelatedKeys(long uuid, long[] previousResolution) {
        return new long[]{
                previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], previousResolution[AbstractKObject.TIME_PREVIOUS_INDEX], uuid,
                previousResolution[AbstractKObject.UNIVERSE_PREVIOUS_INDEX], KConfig.NULL_LONG, uuid,
                KConfig.NULL_LONG, KConfig.NULL_LONG, uuid,
                KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG,
        };
    }

}
