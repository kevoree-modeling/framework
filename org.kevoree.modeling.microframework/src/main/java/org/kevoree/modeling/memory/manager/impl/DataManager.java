package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.resolver.impl.*;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.strategy.KMemoryStrategy;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.scheduler.KScheduler;
import org.kevoree.modeling.operation.impl.HashOperationManager;
import org.kevoree.modeling.operation.KOperationManager;

public class DataManager implements KDataManager, KInternalDataManager {

    private static final String UNIVERSE_NOT_CONNECTED_ERROR = "Please connect your createModel prior to create a universe or an object";

    private final KOperationManager _operationManager;
    private final KContentDeliveryDriver _db;
    private final KScheduler _scheduler;
    private KModel _model;
    private final ListenerManager _listenerManager;
    private final KeyCalculator _modelKeyCalculator;
    private final KResolver _resolver;
    private final KChunkSpace _storage;
    private final KChunkSpaceManager _cache;

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
        this._storage = p_factory.newStorage();
        this._cache = p_factory.newCache(_storage);
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
        KLongLongMap cached = (KLongLongMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
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
        KLongLongMap cached = (KLongLongMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (cached != null) {
            return cached.get(currentUniverseKey);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] descendantsUniverseKeys(final long currentUniverseKey) {
        KLongLongMap cached = (KLongLongMap) _storage.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
        if (cached != null) {
            final ArrayLongLongMap temp = new ArrayLongLongMap(-1, -1, -1, null);
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

    private static final int PREFIX_TO_SAVE_SIZE = 2;
    private static final int KEY_SIZE = 3;

    @Override
    public void save(final KCallback<Throwable> callback) {
        KChunkIterator dirtyIterator = _storage.detachDirties();
        if (dirtyIterator.size() == 0) {
            callback.on(null);
            return;
        }
        long[] toSaveKeys = new long[(dirtyIterator.size() + PREFIX_TO_SAVE_SIZE) * KEY_SIZE];
        String[] toSaveValues = new String[dirtyIterator.size() + PREFIX_TO_SAVE_SIZE];
        int i = 0;
        KMetaModel _mm = _model.metaModel();
        while (dirtyIterator.hasNext()) {
            KChunk loopChunk = dirtyIterator.next();
            toSaveKeys[i * KEY_SIZE] = loopChunk.universe();
            toSaveKeys[i * KEY_SIZE + 1] = loopChunk.time();
            toSaveKeys[i * KEY_SIZE + 2] = loopChunk.obj();
            toSaveValues[i] = loopChunk.serialize(_mm);
            i++;
        }
        toSaveKeys[i * KEY_SIZE] = KConfig.BEGINNING_OF_TIME;
        toSaveKeys[i * KEY_SIZE + 1] = KConfig.NULL_LONG;
        toSaveKeys[i * KEY_SIZE + 2] = _objectKeyCalculator.prefix();
        toSaveValues[i] = "" + _objectKeyCalculator.lastComputedIndex();
        i++;
        toSaveKeys[i * KEY_SIZE] = KConfig.END_OF_TIME;
        toSaveKeys[i * KEY_SIZE + 1] = KConfig.NULL_LONG;
        toSaveKeys[i * KEY_SIZE + 2] = _universeKeyCalculator.prefix();
        toSaveValues[i] = "" + _universeKeyCalculator.lastComputedIndex();
        _db.put(toSaveKeys, toSaveValues, callback, this.currentCdnListener);
    }

    @Override
    public void initKObject(KObject obj) {
        _resolver.indexObject(obj);
    }

    @Override
    public KObjectChunk preciseChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        KObjectChunk resolvedChunk = _resolver.preciseChunk(universe, time, uuid, metaClass, previousResolution);
        if (resolvedChunk != null) {
            return resolvedChunk;
        } else {
            //TODO
            throw new RuntimeException("Cache Miss, not implemented Yet " + universe + "," + time + "," + uuid);
        }
    }

    @Override
    public KObjectChunk closestChunk(long universe, long time, long uuid, KMetaClass metaClass, long[] previousResolution) {
        KObjectChunk resolvedChunk = _resolver.closestChunk(universe, time, uuid, metaClass, previousResolution);
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
                                                        KLongLongMap globalUniverseTree = (KLongLongMap) _cache.createAndMark(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, KChunkTypes.LONG_LONG_MAP);
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
    public void clear() {
        //TODO
    }

    @Override
    public void getRoot(long universe, long time, KCallback<KObject> callback) {
        _resolver.getRoot(universe, time, callback);
    }

    @Override
    public void setRoot(KObject newRoot, KCallback<Throwable> callback) {
        _resolver.setRoot(newRoot, callback);
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
                long[] toLoadKeys = new long[updatedKeys.length];
                int nbElements = updatedKeys.length / KEY_SIZE;
                int toInsertKey = 0;
                for (int i = 0; i < nbElements; i++) {
                    KChunk currentChunk = _cache.getAndMark(updatedKeys[i * 3], updatedKeys[i * 3 + 1], updatedKeys[i * 3 + 2]);
                    if (currentChunk != null) {
                        if ((currentChunk.getFlags() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                            toLoadKeys[toInsertKey * KEY_SIZE] = updatedKeys[i * KEY_SIZE];
                            toLoadKeys[toInsertKey * KEY_SIZE + 1] = updatedKeys[i * KEY_SIZE + 1];
                            toLoadKeys[toInsertKey * KEY_SIZE + 2] = updatedKeys[i * KEY_SIZE + 2];
                            toInsertKey++;
                        }
                        _cache.unmarkMemoryElement(currentChunk);
                    } else if (_listenerManager.isListened(updatedKeys[i * KEY_SIZE + 2]) && updatedKeys[i * KEY_SIZE + 2] != KConfig.NULL_LONG && updatedKeys[i * KEY_SIZE + 1] != KConfig.NULL_LONG) {
                        //check if the object is listened anyway
                        toLoadKeys[toInsertKey * KEY_SIZE] = updatedKeys[i * KEY_SIZE];
                        toLoadKeys[toInsertKey * KEY_SIZE + 1] = updatedKeys[i * KEY_SIZE + 1];
                        toLoadKeys[toInsertKey * KEY_SIZE + 2] = updatedKeys[i * KEY_SIZE + 2];
                        toInsertKey++;
                    }
                }
                if (toInsertKey == 0) {
                    return;
                }
                final long[] trimmedToLoad = new long[toInsertKey];
                System.arraycopy(toLoadKeys, 0, trimmedToLoad, 0, toInsertKey);
                KMetaModel mm = _model.metaModel();
                AbstractKModel am = (AbstractKModel) _model;
                _db.get(trimmedToLoad, new KCallback<String[]>() {
                    @Override
                    public void on(String[] payloads) {
                        KObject[] updatedElements = new KObject[payloads.length];
                        int indexToInsert = 0;
                        for (int i = 0; i < payloads.length; i++) {
                            if (payloads[i] != null) {
                                KChunk currentChunk = _cache.getAndMark(trimmedToLoad[i * 3], trimmedToLoad[i * 3 + 1], trimmedToLoad[i * 3 + 2]);
                                if (currentChunk != null) {
                                    currentChunk.init(payloads[i], mm, -1);
                                    if (currentChunk.type() == KChunkTypes.OBJECT_CHUNK) {
                                        if (_listenerManager.isListened(updatedKeys[i * KEY_SIZE + 2])) {
                                            KObjectChunk objectChunk = (KObjectChunk) currentChunk;
                                            updatedElements[indexToInsert] = am.createProxy(currentChunk.universe(), currentChunk.time(), currentChunk.obj(), mm.metaClass(objectChunk.metaClassIndex()), currentChunk.universe(), currentChunk.time());
                                            indexToInsert++;
                                        } else {
                                            _cache.unmarkMemoryElement(currentChunk);
                                        }
                                    } else {
                                        _cache.unmarkMemoryElement(currentChunk);
                                    }
                                } else {
                                    KObjectChunk objectChunk = (KObjectChunk) _cache.createAndMark(trimmedToLoad[i * 3], trimmedToLoad[i * 3 + 1], trimmedToLoad[i * 3 + 2], KChunkTypes.OBJECT_CHUNK);
                                    objectChunk.init(payloads[i], mm, -1);
                                    updatedElements[indexToInsert] = am.createProxy(objectChunk.universe(), objectChunk.time(), objectChunk.obj(), mm.metaClass(objectChunk.metaClassIndex()), objectChunk.universe(), objectChunk.time());
                                }
                            }
                        }
                        if (indexToInsert == 0) {
                            return;
                        }
                        KObject[] trimmedElements = new KObject[indexToInsert];
                        System.arraycopy(updatedElements, 0, trimmedElements, 0, indexToInsert);
                        _listenerManager.dispatch(trimmedElements);
                    }
                });
            }
        });
    }

    public KOperationManager operationManager() {
        return _operationManager;
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
