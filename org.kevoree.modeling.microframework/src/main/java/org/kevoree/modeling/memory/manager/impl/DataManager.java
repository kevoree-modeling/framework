package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.cache.KCache;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.resolver.impl.*;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.storage.KMemoryStorage;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.operation.impl.HashOperationManager;
import org.kevoree.modeling.operation.KOperationManager;

public class DataManager implements KDataManager, KInternalDataManager {

    private static final String UNIVERSE_NOT_CONNECTED_ERROR = "Please connect your createModel prior to create a universe or an object";

    private final KOperationManager _operationManager;
    private final KContentDeliveryDriver _db;
    private final KScheduler _scheduler;
    private KModel _model;
    private final KMemoryStrategy _factory;
    private final ListenerManager _listenerManager;
    private final KeyCalculator _modelKeyCalculator;
    private final KResolver _resolver;
    private final KMemoryStorage _storage;
    private final KCache _cache;

    private KeyCalculator _objectKeyCalculator = null;
    private KeyCalculator _universeKeyCalculator = null;
    private boolean isConnected = false;

    private Short prefix;

    private static final int UNIVERSE_INDEX = 0;
    private static final int OBJ_INDEX = 1;
    private static final int GLO_TREE_INDEX = 2;
    private static final short zeroPrefix = 0;

    private int currentCdnListener = -1;

    @Override
    public void setModel(KModel p_model) {
        this._model = p_model;
    }

    public DataManager(KContentDeliveryDriver p_cdn, KScheduler p_scheduler, KMemoryStrategy p_factory) {
        this._factory = p_factory;
        this._storage = _factory.newStorage();
        this._cache = _factory.newCache(_storage);
        this._resolver = new DistortedTimeResolver(this._cache, this);
        this._listenerManager = new ListenerManager();
        this._modelKeyCalculator = new KeyCalculator(zeroPrefix, 0);
        this._db = p_cdn;
        this._scheduler = p_scheduler;
        attachContentDeliveryDriver(new MemoryContentDeliveryDriver());
        this._operationManager = new HashOperationManager(this);
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
        return _modelKeyCalculator.nextKey();
    }

    @Override
    public final void initUniverse(KUniverse p_universe, KUniverse p_parent) {
        KUniverseOrderMap cached = (KUniverseOrderMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
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
        KUniverseOrderMap cached = (KUniverseOrderMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (cached != null) {
            return cached.get(currentUniverseKey);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] descendantsUniverseKeys(final long currentUniverseKey) {
        KUniverseOrderMap cached = (KUniverseOrderMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
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
    public synchronized void save(KObject src, final KCallback<Throwable> callback) {
        //TODO //FIXME

        /*
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

                KMetaModel mm = _model.metaModel();

                savedKeys[0] = KContentKey.createObject(src.universe(), src.now(), src.uuid());
                values[0] = cachedObject.serialize(mm);
                cachedObject.setClean(mm);

                int indexToInsert = 1;
                if (cachedObjectTimeTree != null && cachedObjectTimeTree.isDirty()) {
                    savedKeys[indexToInsert] = KContentKey.createTimeTree(src.universe(), src.uuid());
                    values[indexToInsert] = cachedObjectTimeTree.serialize(mm);
                    cachedObjectTimeTree.setClean(mm);
                    indexToInsert++;
                }
                if (cachedObjectUniverseTree != null && cachedObjectUniverseTree.isDirty()) {
                    savedKeys[indexToInsert] = KContentKey.createUniverseTree(src.universe());
                    values[indexToInsert] = cachedObjectUniverseTree.serialize(_model.metaModel());
                    cachedObjectUniverseTree.setClean(mm);
                    indexToInsert++;
                }
                if (cachedObjectGlobalUniverseTree != null && cachedObjectGlobalUniverseTree.isDirty()) {
                    savedKeys[indexToInsert] = KContentKey.createGlobalUniverseTree();
                    values[indexToInsert] = cachedObjectGlobalUniverseTree.serialize(_model.metaModel());
                    cachedObjectGlobalUniverseTree.setClean(mm);
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
        */
    }

    @Override
    public void initKObject(KObject obj) {
        _resolver.indexObject(obj);
    }

    @Override
    public KMemoryChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        KMemoryChunk resolvedChunk = _resolver.preciseChunk(universe, time, uuid, metaClass, previousResolution);
        if (resolvedChunk != null) {
            return resolvedChunk;
        } else {
            //TODO
            throw new RuntimeException("Cache Miss, not implemented Yet " + universe + "," + time + "," + uuid);
        }
    }

    @Override
    public KMemoryChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        KMemoryChunk resolvedChunk = _resolver.closestChunk(universe, time, uuid, metaClass, previousResolution);
        if (resolvedChunk != null) {
            return resolvedChunk;
        } else {
            //TODO
            throw new RuntimeException("Cache Miss, not implemented Yet " + universe + "," + time + "," + uuid);
        }
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
                        _db.atomicGetIncrement(new long[]{KConfig.END_OF_TIME, KConfig.NULL_LONG, KConfig.NULL_LONG},
                                new KCallback<Short>() {
                                    @Override
                                    public void on(Short newPrefix) {
                                        prefix = newPrefix;
                                        long[] connectionKeys = new long[]{
                                                KConfig.BEGINNING_OF_TIME, KConfig.NULL_LONG, newPrefix, //LastUniverseIndexFromPrefix
                                                KConfig.END_OF_TIME, KConfig.NULL_LONG, newPrefix, //LastObjectIndexFromPrefix
                                                KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG //GlobalUniverseTree
                                        };
                                        _db.get(connectionKeys, new KCallback<String[]>() {
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
                                                        KUniverseOrderMap globalUniverseTree = (KUniverseOrderMap) _cache.createAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, KMemoryElementTypes.LONG_LONG_MAP);
                                                        if (globalUniverseTreePayload != null) {
                                                            try {
                                                                globalUniverseTree.init(globalUniverseTreePayload, model().metaModel(), -1);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
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
    public synchronized void discard(KUniverse p_universe, final KCallback<Throwable> callback) {
        //TODO
        //FIXME
        /*

        KContentKey[] toReloadKeys = new KContentKey[1];
        toReloadKeys[0] = KContentKey.createGlobalUniverseTree();
        _db.get(toReloadKeys, new KCallback<String[]>() {
            @Override
            public void on(String[] strings) {
                if (strings != null && strings.length > 0 && strings[0] != null) {
                    KMemoryElement newObject = internal_unserialize(toReloadKeys[0], strings[0]);
                    KMemoryStorage newCache = _factory.newStorage();
                    newCache.getOrPut(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, newObject);
                    //swapCache
                    KMemoryStorage oldCache = _cache;
                    _cache = newCache;
                    oldCache.delete(_model.metaModel());
                    callback.on(null);
                }
            }
        });
        */

    }

    @Override
    public void delete(KUniverse p_universe, KCallback<Throwable> callback) {
        throw new RuntimeException("Not implemented yet !");
    }

    @Override
    public void lookup(long universe, long time, long uuid, KCallback<KObject> callback) {
        this._scheduler.dispatch(this._resolver.lookup(universe, time, uuid, callback));
    }

    @Override
    public void lookupAllObjects(long universe, long time, long[] uuids, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(this._resolver.lookupAllObjects(universe, time, uuids, callback));
    }

    @Override
    public void lookupAllTimes(long universe, long[] times, long uuid, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(this._resolver.lookupAllTimes(universe, times, uuid, callback));
    }

    @Override
    public void lookupAllObjectsTimes(long universe, long[] times, long[] uuids, KCallback<KObject[]> callback) {
        this._scheduler.dispatch(this._resolver.lookupAllObjectsTimes(universe, times, uuids, callback));
    }

    @Override
    public void saveAll(KCallback<Throwable> callback) {
        save(null, callback);
    }

    @Override
    public void discard(KCallback<Throwable> callback) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void getRoot(long universe, long time, KCallback<KObject> callback) {

    }

    @Override
    public void setRoot(KObject newRoot, KCallback<Throwable> callback) {

    }

    @Override
    public KContentDeliveryDriver cdn() {
        return this._db;
    }

    //TODO, check for counter
    private void attachContentDeliveryDriver(KContentDeliveryDriver p_dataBase) {
        currentCdnListener = this._db.addUpdateListener(new KContentUpdateListener() {
            @Override
            public void on(long[] updatedKeys) {
                /*


                KContentKey[] toReloadKey = new KContentKey[updatedKeys.length * 2];
                int indexInsert = 0;
                ArrayLongLongMap tempMap = null;
                int nbDispatch = 0;
                for (int i = 0; i < updatedKeys.length; i++) {
                    //for chunk only we check in all case if there are listened
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
                        //if its a chunk then investigate
                        if (updatedKeys[i].universe != KConfig.NULL_LONG && updatedKeys[i].time != KConfig.NULL_LONG && updatedKeys[i].obj != KConfig.NULL_LONG) {
                            //the chunk has to be loaded anyway, because a listener is waiting for it
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
                */
            }
        });
    }

    public KOperationManager operationManager() {
        return _operationManager;
    }

    @Override
    public void isUsed(KObject origin, boolean state) {

    }

    @Override
    //TODO optimize the classLink
    public void load(long[] keys, KCallback<KMemoryElement[]> callback) {
        this._db.get(keys, new KCallback<String[]>() {
            @Override
            public void on(String[] payloads) {
                KMemoryElement[] results = new KMemoryElement[keys.length / 3];
                for (int i = 0; i < payloads.length; i++) {
                    long loopUniverse = keys[i * 3];
                    long loopTime = keys[i * 3 + 1];
                    long loopUuid = keys[i * 3 + 2];
                    results[i] = _cache.createAndMark(loopUniverse, loopTime, loopUuid, _resolver.typeFromKey(loopUniverse, loopTime, loopUuid));
                    int classIndex = -1;
                    if (loopUniverse != KConfig.NULL_LONG && loopTime != KConfig.NULL_LONG && loopUuid != KConfig.NULL_LONG) {
                        KUniverseOrderMap alreadyLoadedOrder = (KUniverseOrderMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, loopUuid);
                        if (alreadyLoadedOrder != null) {
                            classIndex = _model.metaModel().metaClassByName(alreadyLoadedOrder.metaClassName()).index();
                        }
                    }
                    results[i].init(payloads[i], _model.metaModel(), classIndex);
                }
            }
        });
    }

    @Override
    public KListener createListener(long p_universe) {
        return this._listenerManager.createListener(p_universe);
    }


    @Override
    public void resolveTimes(long currentUniverse, long currentUuid, long startTime, long endTime, KCallback<long[]> callback) {
        _resolver.resolveTimes(currentUniverse, currentUuid, startTime, endTime, callback);
    }
}
