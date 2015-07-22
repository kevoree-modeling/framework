package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.KMemoryFactory;
import org.kevoree.modeling.memory.struct.cache.KCache;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.struct.HeapMemoryFactory;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.operation.impl.HashOperationManager;
import org.kevoree.modeling.operation.KOperationManager;

public class MemoryManager implements KMemoryManager {

    private static final String OUT_OF_CACHE_MESSAGE = "KMF Error: your object is out of cache, you probably kept an old reference. Please reload it with a lookup";
    private static final String UNIVERSE_NOT_CONNECTED_ERROR = "Please connect your model prior to create a universe or an object";

    private KContentDeliveryDriver _db;
    private KOperationManager _operationManager;
    private KScheduler _scheduler;
    private KModel _model;
    private KMemoryFactory _factory;
    private KeyCalculator _objectKeyCalculator = null;
    private KeyCalculator _universeKeyCalculator = null;
    private KeyCalculator _modelKeyCalculator;
    private boolean isConnected = false;
    private KCache _cache;
    private Short prefix;

    private ListenerManager _listenerManager;

    private static final int UNIVERSE_INDEX = 0;
    private static final int OBJ_INDEX = 1;
    private static final int GLO_TREE_INDEX = 2;
    private static final short zeroPrefix = 0;

    private int currentCdnListener = -1;

    public MemoryManager(KModel model) {
        this._listenerManager = new ListenerManager();
        this._factory = new HeapMemoryFactory();
        this._cache = _factory.newCache();
        this._modelKeyCalculator = new KeyCalculator(zeroPrefix, 0);
        setContentDeliveryDriver(new MemoryContentDeliveryDriver());
        this._operationManager = new HashOperationManager(this);
        this._scheduler = new DirectScheduler();
        this._model = model;
    }

    @Override
    public final KCache cache() {
        return _cache;
    }

    @Override
    public final KModel model() {
        return _model;
    }

    @Override
    public final void close(KCallback<Throwable> callback) {
        isConnected = false;
        if (_db != null) {
            _db.close(callback);
        } else {
            callback.on(null);
        }
    }

    /* Key Management Section */
    @Override
    public final long nextUniverseKey() {
        if (_universeKeyCalculator == null) {
            throw new RuntimeException(UNIVERSE_NOT_CONNECTED_ERROR);
        }
        return _universeKeyCalculator.nextKey();
    }

    @Override
    public final long nextObjectKey() {
        if (_objectKeyCalculator == null) {
            throw new RuntimeException(UNIVERSE_NOT_CONNECTED_ERROR);
        }
        return _objectKeyCalculator.nextKey();
    }

    @Override
    public final long nextModelKey() {
        if (_modelKeyCalculator == null) {
            throw new RuntimeException(UNIVERSE_NOT_CONNECTED_ERROR);
        }
        return _modelKeyCalculator.nextKey();
    }

    @Override
    public final void initUniverse(KUniverse p_universe, KUniverse p_parent) {
        KUniverseOrderMap cached = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (cached != null && !cached.contains(p_universe.key())) {
            if (p_parent == null) {
                cached.put(p_universe.key(), p_universe.key());
            } else {
                cached.put(p_universe.key(), p_parent.key());
            }
        }
    }

    @Override
    public long parentUniverseKey(long currentUniverseKey) {
        KUniverseOrderMap cached = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (cached != null) {
            return cached.get(currentUniverseKey);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] descendantsUniverseKeys(final long currentUniverseKey) {
        KUniverseOrderMap cached = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (cached != null) {
            final ArrayLongLongMap temp = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            cached.each(new KLongLongMapCallBack() {
                @Override
                public void on(long key, long value) {
                    if (value == currentUniverseKey && key != currentUniverseKey) {
                        temp.put(key, value);
                    }
                }
            });
            final long[] result = new long[temp.size()];
            final int[] insertIndex = {0};
            temp.each(new KLongLongMapCallBack() {
                @Override
                public void on(long key, long value) {
                    result[insertIndex[0]] = key;
                    insertIndex[0]++;
                }
            });
            return result;
        } else {
            return new long[0];
        }
    }

    @Override
    public void reload(KContentKey[] keys, KCallback<Throwable> callback) {

    }

    @Override
    public synchronized void save(KObject src, final KCallback<Throwable> callback) {
        if (src == null) {
            KContentKey[] dirtyKeys = _cache.dirtyKeys();
            int dirtyKeysSize = dirtyKeys.length;
            KContentKey[] savedKeys = new KContentKey[dirtyKeys.length + 2];
            System.arraycopy(dirtyKeys, 0, savedKeys, 0, dirtyKeysSize);

            String[] values = new String[dirtyKeysSize + 2];
            for (int i = 0; i < dirtyKeysSize; i++) {
                KMemoryElement cachedObject = _cache.get(dirtyKeys[i].universe, dirtyKeys[i].time, dirtyKeys[i].obj);
                if (cachedObject != null) {
                    values[i] = cachedObject.serialize(_model.metaModel());
                    cachedObject.setClean(_model.metaModel());
                } else {
                    values[i] = null;
                }

            }
            savedKeys[dirtyKeysSize] = KContentKey.createLastObjectIndexFromPrefix(_objectKeyCalculator.prefix());
            values[dirtyKeysSize] = "" + _objectKeyCalculator.lastComputedIndex();
            savedKeys[dirtyKeysSize + 1] = KContentKey.createLastUniverseIndexFromPrefix(_universeKeyCalculator.prefix());
            values[dirtyKeysSize + 1] = "" + _universeKeyCalculator.lastComputedIndex();

            _db.put(savedKeys, values, callback, this.currentCdnListener);
        } else {
            KMemoryElement cachedObject = _cache.get(src.universe(), src.now(), src.uuid());
            if (cachedObject == null || !cachedObject.isDirty()) {
                callback.on(null);
            } else {
                KMemoryElement cachedObjectTimeTree = _cache.get(src.universe(), KConfig.NULL_LONG, src.uuid());
                KMemoryElement cachedObjectUniverseTree = _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, src.uuid());
                KMemoryElement cachedObjectGlobalUniverseTree = _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);

                int nbElemToSave = 1;
                if (cachedObjectTimeTree != null && cachedObjectTimeTree.isDirty()) {
                    nbElemToSave++;
                }
                if (cachedObjectUniverseTree != null && cachedObjectUniverseTree.isDirty()) {
                    nbElemToSave++;
                }
                if (cachedObjectGlobalUniverseTree != null && cachedObjectGlobalUniverseTree.isDirty()) {
                    nbElemToSave++;
                }

                KContentKey[] savedKeys = new KContentKey[nbElemToSave + 2];
                String[] values = new String[nbElemToSave + 2];

                savedKeys[0] = KContentKey.createObject(src.universe(), KConfig.NULL_LONG, src.uuid());
                values[0] = cachedObject.serialize(_model.metaModel());

                int indexToInsert = 1;
                if (cachedObjectTimeTree != null && cachedObjectTimeTree.isDirty()) {
                    savedKeys[indexToInsert] = KContentKey.createTimeTree(src.universe(), src.uuid());
                    values[indexToInsert] = cachedObjectTimeTree.serialize(_model.metaModel());
                    indexToInsert++;
                }
                if (cachedObjectUniverseTree != null && cachedObjectUniverseTree.isDirty()) {
                    savedKeys[indexToInsert] = KContentKey.createUniverseTree(src.universe());
                    values[indexToInsert] = cachedObjectUniverseTree.serialize(_model.metaModel());
                    indexToInsert++;
                }
                if (cachedObjectGlobalUniverseTree != null && cachedObjectGlobalUniverseTree.isDirty()) {
                    savedKeys[indexToInsert] = KContentKey.createGlobalUniverseTree();
                    values[indexToInsert] = cachedObjectGlobalUniverseTree.serialize(_model.metaModel());
                    indexToInsert++;
                }
                savedKeys[indexToInsert] = KContentKey.createLastObjectIndexFromPrefix(_objectKeyCalculator.prefix());
                values[indexToInsert] = "" + _objectKeyCalculator.lastComputedIndex();
                indexToInsert++;
                savedKeys[indexToInsert] = KContentKey.createLastUniverseIndexFromPrefix(_universeKeyCalculator.prefix());
                values[indexToInsert] = "" + _universeKeyCalculator.lastComputedIndex();

                _db.put(savedKeys, values, callback, this.currentCdnListener);
            }
        }
    }

    @Override
    public void initKObject(KObject obj) {
        KMemorySegment cacheEntry = _factory.newCacheSegment();
        cacheEntry.initMetaClass(obj.metaClass());
        cacheEntry.init(null, model().metaModel());
        cacheEntry.setDirty();
        cacheEntry.inc();
        //initiate time management
        KLongTree timeTree = _factory.newLongTree();
        timeTree.init(null, model().metaModel());
        timeTree.inc();
        timeTree.insert(obj.now());
        //initiate universe management
        KUniverseOrderMap universeTree = _factory.newUniverseMap(0, obj.metaClass().metaName());
        universeTree.init(null, model().metaModel());
        universeTree.inc();
        universeTree.put(obj.universe(), obj.now());
        //save related objects to cache
        _cache.getOrPut(obj.universe(), KConfig.NULL_LONG, obj.uuid(), timeTree);
        _cache.getOrPut(KConfig.NULL_LONG, KConfig.NULL_LONG, obj.uuid(), universeTree);
        _cache.getOrPut(obj.universe(), obj.now(), obj.uuid(), cacheEntry);
    }

    @Override
    public void connect(final KCallback<Throwable> connectCallback) {
        if (isConnected) {
            if (connectCallback != null) {
                connectCallback.on(null);
            }
        }
        if (_db == null) {
            if (connectCallback != null) {
                connectCallback.on(new Exception("Please attach a KDataBase AND a KBroker first !"));
            }
        } else {
            _db.connect(new KCallback<Throwable>() {
                @Override
                public void on(Throwable throwable) {
                    if (throwable == null) {
                        _db.atomicGetIncrement(KContentKey.createLastPrefix(),
                                new KCallback<Short>() {
                                    @Override
                                    public void on(Short newPrefix) {
                                        KContentKey[] connectionElemKeys = new KContentKey[3];
                                        connectionElemKeys[UNIVERSE_INDEX] = KContentKey.createLastUniverseIndexFromPrefix(newPrefix);
                                        connectionElemKeys[OBJ_INDEX] = KContentKey.createLastObjectIndexFromPrefix(newPrefix);
                                        connectionElemKeys[GLO_TREE_INDEX] = KContentKey.createGlobalUniverseTree();
                                        prefix = newPrefix;
                                        _db.get(connectionElemKeys, new KCallback<String[]>() {
                                            @Override
                                            public void on(String[] strings) {
                                                if (strings.length == 3) {
                                                    Exception detected = null;
                                                    try {
                                                        String uniIndexPayload = strings[UNIVERSE_INDEX];
                                                        if (uniIndexPayload == null || uniIndexPayload.equals("")) {
                                                            uniIndexPayload = "0";
                                                        }
                                                        String objIndexPayload = strings[OBJ_INDEX];
                                                        if (objIndexPayload == null || objIndexPayload.equals("")) {
                                                            objIndexPayload = "0";
                                                        }
                                                        String globalUniverseTreePayload = strings[GLO_TREE_INDEX];
                                                        KUniverseOrderMap globalUniverseTree;
                                                        if (globalUniverseTreePayload != null) {
                                                            globalUniverseTree = _factory.newUniverseMap(0, null);
                                                            try {
                                                                globalUniverseTree.init(globalUniverseTreePayload, model().metaModel());
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            globalUniverseTree = _factory.newUniverseMap(KConfig.CACHE_INIT_SIZE, null);
                                                        }
                                                        _cache.getOrPut(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, globalUniverseTree);
                                                        long newUniIndex = Long.parseLong(uniIndexPayload);
                                                        long newObjIndex = Long.parseLong(objIndexPayload);
                                                        _universeKeyCalculator = new KeyCalculator(prefix, newUniIndex);
                                                        _objectKeyCalculator = new KeyCalculator(prefix, newObjIndex);
                                                        isConnected = true;
                                                    } catch (Exception e) {
                                                        detected = e;
                                                    }
                                                    if (connectCallback != null) {
                                                        connectCallback.on(detected);
                                                    }
                                                } else {
                                                    if (connectCallback != null) {
                                                        connectCallback.on(new Exception("Error while connecting the KDataStore..."));
                                                    }
                                                }

                                            }
                                        });

                                    }
                                });
                    } else {
                        if (connectCallback != null) {
                            connectCallback.on(throwable);
                        }
                    }
                }
            });
        }
    }

    @Override
    public KMemorySegment segment(long universe, long requestedTime, long uuid, boolean resolvePreviousSegment, KMetaClass metaClass, KMemorySegmentResolutionTrace resolutionTrace) {
        long time = requestedTime;
        if (metaClass.temporalResolution() != 1) {
            time = time - (time % metaClass.temporalResolution());
        }
        KMemorySegment currentEntry = (KMemorySegment) _cache.get(universe, time, uuid);
        if (currentEntry != null) {
            if (resolutionTrace != null) {
                resolutionTrace.setSegment(currentEntry);
                resolutionTrace.setUniverse(universe);
                resolutionTrace.setTime(time);
                resolutionTrace.setUniverseOrder((KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid));
                resolutionTrace.setTimeTree((KLongTree) _cache.get(universe, KConfig.NULL_LONG, uuid));
            }
            return currentEntry;
        }
        KUniverseOrderMap objectUniverseTree = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, uuid);
        KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        long resolvedUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, objectUniverseTree, time, universe);
        KLongTree timeTree = (KLongTree) _cache.get(resolvedUniverse, KConfig.NULL_LONG, uuid);
        if (timeTree == null) {
            throw new RuntimeException(OUT_OF_CACHE_MESSAGE + " : TimeTree not found for " + KContentKey.createTimeTree(resolvedUniverse, uuid) + " from " + universe + "/" + resolvedUniverse);
        }
        long resolvedTime = timeTree.previousOrEqual(time);
        if (resolutionTrace != null) {
            resolutionTrace.setUniverse(resolvedUniverse);
            resolutionTrace.setTime(resolvedTime);
            resolutionTrace.setUniverseOrder(objectUniverseTree);
            resolutionTrace.setTimeTree(timeTree);
        }
        if (resolvedTime != KConfig.NULL_LONG) {
            boolean needTimeCopy = !resolvePreviousSegment && (resolvedTime != time);
            boolean needUniverseCopy = !resolvePreviousSegment && (resolvedUniverse != universe);
            KMemorySegment entry = (KMemorySegment) _cache.get(resolvedUniverse, resolvedTime, uuid);
            if (entry == null) {
                return null;
            }
            if (!needTimeCopy && !needUniverseCopy) {
                if (!resolvePreviousSegment) {
                    entry.setDirty();
                }
                if (resolutionTrace != null) {
                    resolutionTrace.setSegment(entry);
                }
                return entry;
            } else {
                KMemorySegment clonedEntry = entry.clone(metaClass);
                clonedEntry = (KMemorySegment) _cache.getOrPut(universe, time, uuid, clonedEntry);
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
                if (resolutionTrace != null) {
                    resolutionTrace.setSegment(clonedEntry);
                }
                return clonedEntry;
            }
        } else {
            System.err.println(OUT_OF_CACHE_MESSAGE + " Time not resolved " + time);
            return null;
        }
    }

    @Override
    public synchronized void discard(KUniverse p_universe, final KCallback<Throwable> callback) {
        KContentKey[] toReloadKeys = new KContentKey[1];
        toReloadKeys[0] = KContentKey.createGlobalUniverseTree();
        _db.get(toReloadKeys, new KCallback<String[]>() {
            @Override
            public void on(String[] strings) {
                if (strings != null && strings.length > 0 && strings[0] != null) {
                    KMemoryElement newObject = internal_unserialize(toReloadKeys[0], strings[0]);
                    KCache newCache = _factory.newCache();
                    newCache.getOrPut(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, newObject);
                    //swapCache
                    KCache oldCache = _cache;
                    _cache = newCache;
                    oldCache.delete(_model.metaModel());
                    callback.on(null);
                }
            }
        });
    }

    @Override
    public void delete(KUniverse p_universe, KCallback<Throwable> callback) {
        throw new RuntimeException("Not implemented yet !");
    }

    @Override
    public void lookup(long universe, long time, long uuid, KCallback<KObject> callback) {
        long[] keys = new long[1];
        keys[0] = uuid;
        lookupAllObjects(universe, time, keys, new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                if (kObjects.length == 1) {
                    if (callback != null) {
                        callback.on(kObjects[0]);
                    }
                } else {
                    if (callback != null) {
                        callback.on(null);
                    }
                }
            }
        });
    }

    @Override
    public void lookupAllObjects(long universe, long time, long[] uuids, final KCallback<KObject[]> callback) {
        this._scheduler.dispatch(new LookupAllObjectsRunnable(universe, time, uuids, callback, this));
    }

    @Override
    public void lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(new LookupAllTimesRunnable(universe, times, uuid, callback, this));
    }

    @Override
    public void lookupAllObjectsTimes(long universe, long[] times, long[] uuid, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(new LookupAllObjectsTimesRunnable(universe, times, uuid, callback, this));
    }

    @Override
    public KContentDeliveryDriver cdn() {
        return this._db;
    }

    @Override
    public void setContentDeliveryDriver(KContentDeliveryDriver p_dataBase) {
        this._db = p_dataBase;
        currentCdnListener = this._db.addUpdateListener(new KContentUpdateListener() {
            @Override
            public void on(KContentKey[] updatedKeys) {
                KContentKey[] toReloadKey = new KContentKey[updatedKeys.length * 2];
                int indexInsert = 0;
                ArrayLongLongMap tempMap = null;
                int nbDispatch = 0;
                for (int i = 0; i < updatedKeys.length; i++) {
                    //for segment only we check in all case if there are listened
                    if (updatedKeys[i].universe != KConfig.NULL_LONG && updatedKeys[i].time != KConfig.NULL_LONG && updatedKeys[i].obj != KConfig.NULL_LONG) {
                        if (_listenerManager.isListened(updatedKeys[i])) {
                            //if yes, tag it as event for KListener
                            nbDispatch++;
                            if (tempMap == null) {
                                tempMap = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                            }
                            //check that the KUniverseTree will be ready
                            if (!tempMap.contains(updatedKeys[i].obj)) {
                                KUniverseOrderMap alreadyLoadedOrder = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, updatedKeys[i].obj);
                                if (alreadyLoadedOrder == null) {
                                    toReloadKey[indexInsert] = KContentKey.createUniverseTree(updatedKeys[i].obj);
                                    indexInsert++;
                                    tempMap.put(updatedKeys[i].obj, updatedKeys[i].obj);
                                }
                            }
                        }
                    }
                    KMemoryElement cached = _cache.get(updatedKeys[i].universe, updatedKeys[i].time, updatedKeys[i].obj);
                    //first we check if the object is already in cache
                    if (cached == null) {
                        //if its a segment then investigate
                        if (updatedKeys[i].universe != KConfig.NULL_LONG && updatedKeys[i].time != KConfig.NULL_LONG && updatedKeys[i].obj != KConfig.NULL_LONG) {
                            //the segment has to be loaded anyway, because a listener is waiting for it
                            if (_listenerManager.isListened(updatedKeys[i])) {
                                toReloadKey[indexInsert] = updatedKeys[i];
                                indexInsert++;
                            }
                        }
                    } else {
                        //check if the element is dirty, otherwise wait
                        if (!cached.isDirty()) {
                            //this is a UniverseTree, tag as reloaded to avoid duplicate
                            if (updatedKeys[i].universe == KConfig.NULL_LONG && updatedKeys[i].time == KConfig.NULL_LONG) {
                                if (tempMap == null) {
                                    tempMap = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                                }
                                //reload if not already tagged as reload
                                if (!tempMap.contains(updatedKeys[i].obj)) {
                                    tempMap.put(updatedKeys[i].obj, updatedKeys[i].obj);
                                    toReloadKey[indexInsert] = updatedKeys[i];
                                    indexInsert++;
                                }
                                //otherwise just ask for reload
                            } else {
                                toReloadKey[indexInsert] = updatedKeys[i];
                                indexInsert++;
                            }
                        }
                    }
                }
                if (indexInsert == 0) {
                    return;
                }
                final KContentKey[] pruned2ReloadKey = new KContentKey[indexInsert];
                System.arraycopy(toReloadKey, 0, pruned2ReloadKey, 0, indexInsert);
                final int finalNbDispatch = nbDispatch;
                _db.get(pruned2ReloadKey, new KCallback<String[]>() {
                    @Override
                    public void on(String[] strings) {
                        KObject[] updatedElems = new KObject[finalNbDispatch];
                        KContentKey[] correspondingKeys = new KContentKey[finalNbDispatch];
                        int insertUpdatedKeys = 0;
                        for (int i = 0; i < strings.length; i++) {
                            if (strings[i] != null) {
                                KContentKey correspondingKey = pruned2ReloadKey[i];
                                KMemoryElement cachedObj = _cache.get(correspondingKey.universe, correspondingKey.time, correspondingKey.obj);
                                if (cachedObj != null && !cachedObj.isDirty()) {
                                    cachedObj = internal_unserialize(correspondingKey, strings[i]);
                                    if (cachedObj != null) {
                                        //replace the cache value
                                        _cache.putAndReplace(correspondingKey.universe, correspondingKey.time, correspondingKey.obj, cachedObj);
                                    }
                                }
                                if (correspondingKey.universe != KConfig.NULL_LONG && correspondingKey.time != KConfig.NULL_LONG && correspondingKey.obj != KConfig.NULL_LONG) {
                                    if (_listenerManager.isListened(updatedKeys[i])) {
                                        KUniverseOrderMap alreadyLoadedOrder = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, updatedKeys[i].obj);
                                        if (alreadyLoadedOrder != null) {
                                            correspondingKeys[insertUpdatedKeys] = correspondingKey;
                                            updatedElems[insertUpdatedKeys] = ((AbstractKModel) _model).createProxy(correspondingKey.universe, correspondingKey.time, correspondingKey.obj, _model.metaModel().metaClassByName(alreadyLoadedOrder.metaClassName()));
                                            insertUpdatedKeys++;
                                        }
                                    }
                                }

                            }
                        }
                        //now dispatch to listeners
                        _listenerManager.dispatch(correspondingKeys, updatedElems, insertUpdatedKeys);
                    }
                });
            }
        });
    }

    @Override
    public void setScheduler(KScheduler p_scheduler) {
        if (p_scheduler != null) {
            this._scheduler = p_scheduler;
        }
    }

    public KOperationManager operationManager() {
        return _operationManager;
    }

    /* Special case management for Root */
    public void getRoot(long universe, long time, final KCallback<KObject> callback) {
        bumpKeyToCache(KContentKey.createRootUniverseTree(), new KCallback<KMemoryElement>() {
            @Override
            public void on(KMemoryElement rootGlobalUniverseIndex) {
                if (rootGlobalUniverseIndex == null) {
                    callback.on(null);
                } else {
                    KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
                    long closestUniverse = ResolutionHelper.resolve_universe(globalUniverseTree, (KUniverseOrderMap) rootGlobalUniverseIndex, time, universe);
                    KContentKey universeTreeRootKey = KContentKey.createRootTimeTree(closestUniverse);
                    bumpKeyToCache(universeTreeRootKey, new KCallback<KMemoryElement>() {
                        @Override
                        public void on(KMemoryElement universeTree) {
                            if (universeTree == null) {
                                callback.on(null);
                            } else {
                                long resolvedVal = ((KLongLongTree) universeTree).previousOrEqualValue(time);
                                if (resolvedVal == KConfig.NULL_LONG) {
                                    callback.on(null);
                                } else {
                                    lookup(universe, time, resolvedVal, callback);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

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
    /* End of root section */

    @Override
    public void cleanCache() {
        if (_cache != null) {
            _cache.clean(_model.metaModel());
        }
    }

    @Override
    public void setFactory(KMemoryFactory p_factory) {
        this._factory = p_factory;
        this._cache = _factory.newCache();
    }

    @Override
    public KListener createListener(long p_universe) {
        return this._listenerManager.createListener(p_universe);
    }

    public void bumpKeyToCache(final KContentKey contentKey, final KCallback<KMemoryElement> callback) {
        KMemoryElement cached = _cache.get(contentKey.universe, contentKey.time, contentKey.obj);
        if (cached != null) {
            callback.on(cached);
        } else {
            KContentKey[] keys = new KContentKey[1];
            keys[0] = contentKey;
            _db.get(keys, new KCallback<String[]>() {
                @Override
                public void on(String[] strings) {
                    if (strings[0] != null) {
                        KMemoryElement newObject = internal_unserialize(contentKey, strings[0]);
                        if (newObject != null) {
                            newObject = _cache.getOrPut(contentKey.universe, contentKey.time, contentKey.obj, newObject);
                        }
                        callback.on(newObject);
                    } else {
                        callback.on(null);
                    }
                }
            });
        }
    }

    public void bumpKeysToCache(KContentKey[] contentKeys, final KCallback<KMemoryElement[]> callback) {
        boolean[] toLoadIndexes = null;
        int nbElem = 0;
        final KMemoryElement[] result = new KMemoryElement[contentKeys.length];
        for (int i = 0; i < contentKeys.length; i++) {
            if (contentKeys[i] != null) {
                result[i] = _cache.get(contentKeys[i].universe, contentKeys[i].time, contentKeys[i].obj);
                if (result[i] == null) {
                    if (toLoadIndexes == null) {
                        toLoadIndexes = new boolean[contentKeys.length];
                    }
                    toLoadIndexes[i] = true;
                    nbElem++;
                }
            }
        }
        if (toLoadIndexes == null) {
            callback.on(result);
        } else {
            final KContentKey[] toLoadDbKeys = new KContentKey[nbElem];
            final int[] originIndexes = new int[nbElem];
            int toLoadIndex = 0;
            for (int i = 0; i < contentKeys.length; i++) {
                if (toLoadIndexes[i]) {
                    toLoadDbKeys[toLoadIndex] = contentKeys[i];
                    originIndexes[toLoadIndex] = i;
                    toLoadIndex++;
                }
            }
            _db.get(toLoadDbKeys, new KCallback<String[]>() {
                @Override
                public void on(String[] payloads) {
                    for (int i = 0; i < payloads.length; i++) {
                        if (payloads[i] != null) {
                            KContentKey newObjKey = toLoadDbKeys[i];
                            KMemoryElement newObject = internal_unserialize(newObjKey, payloads[i]);
                            if (newObject != null) {
                                newObject = _cache.getOrPut(newObjKey.universe, newObjKey.time, newObjKey.obj, newObject);
                                int originIndex = originIndexes[i];
                                result[originIndex] = newObject;
                            }
                        }
                    }
                    callback.on(result);
                }
            });
        }
    }

    /* This method init objects according to KContentKey specification */
    private KMemoryElement internal_unserialize(KContentKey key, String payload) {
        KMemoryElement newElement = _factory.newFromKey(key.universe, key.time, key.obj);
        try {
            if (key.universe != KConfig.NULL_LONG && key.time != KConfig.NULL_LONG && key.obj != KConfig.NULL_LONG) {
                KUniverseOrderMap alreadyLoadedOrder = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, key.obj);
                if (alreadyLoadedOrder != null) {
                    ((KMemorySegment) newElement).initMetaClass(_model.metaModel().metaClassByName(alreadyLoadedOrder.metaClassName()));
                }
            }
            newElement.init(payload, model().metaModel());
            newElement.setClean(model().metaModel());
            return newElement;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
