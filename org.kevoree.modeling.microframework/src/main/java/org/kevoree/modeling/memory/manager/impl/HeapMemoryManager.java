package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.*;
import org.kevoree.modeling.memory.cache.impl.HashMemoryCache;
import org.kevoree.modeling.cdn.impl.ContentPutRequest;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.manager.AccessMode;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.struct.HeapMemoryFactory;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment;
import org.kevoree.modeling.message.impl.Events;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;
import org.kevoree.modeling.memory.struct.tree.impl.LongLongTree;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.operation.impl.HashOperationManager;
import org.kevoree.modeling.operation.KOperationManager;
import org.kevoree.modeling.memory.cache.impl.KCacheDirty;

import java.util.ArrayList;
import java.util.List;

public class HeapMemoryManager implements KMemoryManager {

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
    private KeyCalculator _groupKeyCalculator;
    private boolean isConnected = false;
    private KCache _cache;

    private static final int UNIVERSE_INDEX = 0;
    private static final int OBJ_INDEX = 1;
    private static final int GLO_TREE_INDEX = 2;
    private static final short zeroPrefix = 0;

    public HeapMemoryManager(KModel model) {
        this._cache = new HashMemoryCache();
        this._modelKeyCalculator = new KeyCalculator(zeroPrefix, 0);
        this._groupKeyCalculator = new KeyCalculator(zeroPrefix, 0);
        this._db = new MemoryContentDeliveryDriver();
        this._db.setManager(this);
        this._operationManager = new HashOperationManager(this);
        this._scheduler = new DirectScheduler();
        this._model = model;
        this._factory = new HeapMemoryFactory();
    }

    @Override
    public KCache cache() {
        return _cache;
    }

    @Override
    public KModel model() {
        return _model;
    }

    @Override
    public void close(KCallback<Throwable> callback) {
        isConnected = false;
        if (_db != null) {
            _db.close(callback);
        } else {
            callback.on(null);
        }
    }

    /* Key Management Section */
    @Override
    public synchronized long nextUniverseKey() {
        if (_universeKeyCalculator == null) {
            throw new RuntimeException(UNIVERSE_NOT_CONNECTED_ERROR);
        }
        long nextGeneratedKey = _universeKeyCalculator.nextKey();
        if (nextGeneratedKey == KConfig.NULL_LONG || nextGeneratedKey == KConfig.END_OF_TIME) {
            nextGeneratedKey = _universeKeyCalculator.nextKey();
        }
        return nextGeneratedKey;
    }

    @Override
    public synchronized long nextObjectKey() {
        if (_objectKeyCalculator == null) {
            throw new RuntimeException(UNIVERSE_NOT_CONNECTED_ERROR);
        }
        long nextGeneratedKey = _objectKeyCalculator.nextKey();
        if (nextGeneratedKey == KConfig.NULL_LONG || nextGeneratedKey == KConfig.END_OF_TIME) {
            nextGeneratedKey = _objectKeyCalculator.nextKey();
        }
        return nextGeneratedKey;
    }

    @Override
    public synchronized long nextModelKey() {
        return _modelKeyCalculator.nextKey();
    }

    @Override
    public synchronized long nextGroupKey() {
        return _groupKeyCalculator.nextKey();
    }

    /* End Key Management Section */
    public KUniverseOrderMap globalUniverseOrder() {
        return (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
    }

    @Override
    public void initUniverse(KUniverse p_universe, KUniverse p_parent) {
        KLongLongMap cached = globalUniverseOrder();
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
        KLongLongMap cached = globalUniverseOrder();
        if (cached != null) {
            return cached.get(currentUniverseKey);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] descendantsUniverseKeys(final long currentUniverseKey) {
        KLongLongMap cached = globalUniverseOrder();
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
    public synchronized void save(final KCallback<Throwable> callback) {
        KCacheDirty[] dirtiesEntries = _cache.dirties();
        ContentPutRequest request = new ContentPutRequest(dirtiesEntries.length + 2);
        final Events notificationMessages = new Events(dirtiesEntries.length);
        for (int i = 0; i < dirtiesEntries.length; i++) {
            KMemoryElement cachedObject = dirtiesEntries[i].object;
            int[] meta;
            if (dirtiesEntries[i].object instanceof HeapMemorySegment) {
                HeapMemorySegment segment = (HeapMemorySegment) dirtiesEntries[i].object;
                meta = segment.modifiedIndexes(_model.metaModel().metaClasses()[segment.metaClassIndex()]);
            } else {
                meta = null;
            }
            notificationMessages.setEvent(i, dirtiesEntries[i].key, meta);
            request.put(dirtiesEntries[i].key, cachedObject.serialize(_model.metaModel()));
            cachedObject.setClean(_model.metaModel());
        }
        request.put(KContentKey.createLastObjectIndexFromPrefix(_objectKeyCalculator.prefix()), "" + _objectKeyCalculator.lastComputedIndex());
        request.put(KContentKey.createLastUniverseIndexFromPrefix(_universeKeyCalculator.prefix()), "" + _universeKeyCalculator.lastComputedIndex());
        _db.put(request, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable == null) {
                    _db.send(notificationMessages);
                }
                if (callback != null) {
                    callback.on(throwable);
                }
            }
        });
    }

    @Override
    public void initKObject(KObject obj) {
        KMemorySegment cacheEntry = _factory.newCacheSegment();
        cacheEntry.initMetaClass(obj.metaClass());
        cacheEntry.setDirty();
        cacheEntry.inc();
        //initiate time management
        KLongTree timeTree = _factory.newLongTree();
        timeTree.inc();
        timeTree.insert(obj.now());
        //initiate universe management
        KUniverseOrderMap universeTree = _factory.newUniverseMap(0, obj.metaClass().metaName());
        universeTree.inc();
        universeTree.put(obj.universe(), obj.now());
        //save related objects to cache
        _cache.put(obj.universe(), KConfig.NULL_LONG, obj.uuid(), timeTree);
        _cache.put(KConfig.NULL_LONG, KConfig.NULL_LONG, obj.uuid(), universeTree);
        _cache.put(obj.universe(), obj.now(), obj.uuid(), cacheEntry);
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
                                        final Short finalNewPrefix = newPrefix;
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
                                                        _cache.put(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, globalUniverseTree);
                                                        long newUniIndex = Long.parseLong(uniIndexPayload);
                                                        long newObjIndex = Long.parseLong(objIndexPayload);
                                                        _universeKeyCalculator = new KeyCalculator(finalNewPrefix, newUniIndex);
                                                        _objectKeyCalculator = new KeyCalculator(finalNewPrefix, newObjIndex);
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
    public KMemorySegment segment(long universe, long time, long uuid, AccessMode accessMode, KMetaClass metaClass, KMemorySegmentResolutionTrace resolutionTrace) {
        HeapMemorySegment currentEntry = (HeapMemorySegment) _cache.get(universe, time, uuid);
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
        long resolvedUniverse = ResolutionHelper.resolve_universe(globalUniverseOrder(), objectUniverseTree, time, universe);
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
            boolean needTimeCopy = accessMode.equals(AccessMode.NEW) && (resolvedTime != time);
            boolean needUniverseCopy = accessMode.equals(AccessMode.NEW) && (resolvedUniverse != universe);
            HeapMemorySegment entry = (HeapMemorySegment) _cache.get(resolvedUniverse, resolvedTime, uuid);
            if (entry == null) {
                return null;
            }
            if (accessMode.equals(AccessMode.DELETE)) {
                timeTree.delete(time);
                if (resolutionTrace != null) {
                    resolutionTrace.setSegment(entry);
                }
                return entry;
            }
            if (!needTimeCopy && !needUniverseCopy) {
                if (accessMode.equals(AccessMode.NEW)) {
                    entry.setDirty();
                }
                if (resolutionTrace != null) {
                    resolutionTrace.setSegment(entry);
                }
                return entry;
            } else {
                KMemorySegment clonedEntry = entry.clone(metaClass);
                _cache.put(universe, time, uuid, clonedEntry);
                if (!needUniverseCopy) {
                    timeTree.insert(time);
                } else {
                    KLongTree newTemporalTree = _factory.newLongTree();
                    newTemporalTree.insert(time);
                    newTemporalTree.inc();
                    timeTree.dec();
                    _cache.put(universe, KConfig.NULL_LONG, uuid, newTemporalTree);
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
    public void discard(KUniverse p_universe, final KCallback<Throwable> callback) {
        //save prefix to not negociate again prefix
        _cache.clear(_model.metaModel());
        KContentKey[] globalUniverseTree = new KContentKey[1];
        globalUniverseTree[0] = KContentKey.createGlobalUniverseTree();
        reload(globalUniverseTree, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                callback.on(throwable);
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
        lookupAllobjects(universe, time, keys, new KCallback<KObject[]>() {
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
    public void lookupAllobjects(long universe, long time, long[] uuids, final KCallback<KObject[]> callback) {
        this._scheduler.dispatch(new LookupAllRunnable(universe, time, uuids, callback, this));
    }

    @Override
    public void lookupAlltimes(long universe, long[] time, long uuid, KCallback<KObject[]> callback) {
        throw new RuntimeException("Not Implemented Yet !");
        //this._scheduler.dispatch(new LookupAllRunnable(universe,time, uuids, callback, this));
    }

    @Override
    public KContentDeliveryDriver cdn() {
        return this._db;
    }

    @Override
    public void setContentDeliveryDriver(KContentDeliveryDriver p_dataBase) {
        this._db = p_dataBase;
        p_dataBase.setManager(this);
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
                    long closestUniverse = ResolutionHelper.resolve_universe(globalUniverseOrder(), (KUniverseOrderMap) rootGlobalUniverseIndex, time, universe);
                    KContentKey universeTreeRootKey = KContentKey.createRootTimeTree(closestUniverse);
                    bumpKeyToCache(universeTreeRootKey, new KCallback<KMemoryElement>() {
                        @Override
                        public void on(KMemoryElement universeTree) {
                            if (universeTree == null) {
                                callback.on(null);
                            } else {
                                long resolvedVal = ((LongLongTree) universeTree).previousOrEqualValue(time);
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
                    _cache.put(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.END_OF_TIME, cleanedTree);
                }
                long closestUniverse = ResolutionHelper.resolve_universe(globalUniverseOrder(), cleanedTree, newRoot.now(), newRoot.universe());
                cleanedTree.put(newRoot.universe(), newRoot.now());
                if (closestUniverse != newRoot.universe()) {
                    KLongLongTree newTimeTree = _factory.newLongLongTree();
                    newTimeTree.insert(newRoot.now(), newRoot.uuid());
                    KContentKey universeTreeRootKey = KContentKey.createRootTimeTree(newRoot.universe());
                    _cache.put(universeTreeRootKey.universe, universeTreeRootKey.time, universeTreeRootKey.obj, (KMemoryElement) newTimeTree);
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
                                _cache.put(universeTreeRootKey.universe, universeTreeRootKey.time, universeTreeRootKey.obj, (KMemoryElement) initializedTree);
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
    public void reload(KContentKey[] keys, final KCallback<Throwable> callback) {
        List<KContentKey> toReload = new ArrayList<KContentKey>();
        for (int i = 0; i < keys.length; i++) {
            KMemoryElement cached = _cache.get(keys[i].universe, keys[i].time, keys[i].obj);
            if (cached != null && !cached.isDirty()) {
                toReload.add(keys[i]);
            }
        }
        final KContentKey[] toReload_flat = toReload.toArray(new KContentKey[toReload.size()]);
        _db.get(toReload_flat, new KCallback<String[]>() {
            @Override
            public void on(String[] strings) {
                for (int i = 0; i < strings.length; i++) {
                    if (strings[i] != null) {
                        KContentKey correspondingKey = toReload_flat[i];
                        KMemoryElement cachedObj = _cache.get(correspondingKey.universe, correspondingKey.time, correspondingKey.obj);
                        if (cachedObj != null && !cachedObj.isDirty()) {
                            cachedObj = internal_unserialize(correspondingKey, strings[i]);
                            if (cachedObj != null) {
                                //replace the cache value
                                _cache.put(correspondingKey.universe, correspondingKey.time, correspondingKey.obj, cachedObj);
                            }
                        }
                    }
                }
                if (callback != null) {
                    callback.on(null);
                }
            }
        });
    }

    @Override
    public void cleanCache() {
        if (_cache != null) {
            _cache.clean(_model.metaModel());
        }
    }

    @Override
    public void setFactory(KMemoryFactory p_factory) {
        this._factory = p_factory;
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
                            _cache.put(contentKey.universe, contentKey.time, contentKey.obj, newObject);
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
                                _cache.put(newObjKey.universe, newObjKey.time, newObjKey.obj, newObject);
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
            if(key.universe != KConfig.NULL_LONG && key.time != KConfig.NULL_LONG && key.obj != KConfig.NULL_LONG){
                KUniverseOrderMap alreadyLoadedOrder = (KUniverseOrderMap) _cache.get(KConfig.NULL_LONG,KConfig.NULL_LONG,key.obj);
                if(alreadyLoadedOrder != null){
                    ((KMemorySegment)newElement).initMetaClass(_model.metaModel().metaClassByName(alreadyLoadedOrder.metaClassName()));
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
