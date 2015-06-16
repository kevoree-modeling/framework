package org.kevoree.modeling.event.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KUniverse;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.memory.manager.impl.HeapMemoryManager;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.manager.impl.KeyCalculator;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap;
import org.kevoree.modeling.memory.struct.map.KLongMapCallBack;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.message.impl.Events;
import org.kevoree.modeling.message.KMessage;

public class LocalEventListeners {

    private KMemoryManager _manager;

    private KeyCalculator _internalListenerKeyGen;

    private ArrayLongMap<KEventListener> _simpleListener;

    private ArrayLongMap<KEventMultiListener> _multiListener;

    private ArrayLongLongMap _listener2Object;

    private ArrayLongMap<long[]> _listener2Objects;

    private ArrayLongMap<ArrayLongLongMap> _obj2Listener;

    private ArrayLongMap<ArrayLongLongMap> _group2Listener;

    public LocalEventListeners() {
        _internalListenerKeyGen = new KeyCalculator((short) 0, 0);
        _simpleListener = new ArrayLongMap<KEventListener>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _multiListener = new ArrayLongMap<KEventMultiListener>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _obj2Listener = new ArrayLongMap<ArrayLongLongMap>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _listener2Object = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _listener2Objects = new ArrayLongMap<long[]>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _group2Listener = new ArrayLongMap<ArrayLongLongMap>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    public synchronized void registerListener(long groupId, KObject origin, KEventListener listener) {
        long generateNewID = _internalListenerKeyGen.nextKey();
        _simpleListener.put(generateNewID, listener);
        _listener2Object.put(generateNewID, origin.universe());
        ArrayLongLongMap subLayer = _obj2Listener.get(origin.uuid());
        if (subLayer == null) {
            subLayer = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            _obj2Listener.put(origin.uuid(), subLayer);
        }
        subLayer.put(generateNewID, origin.universe());
        subLayer = _group2Listener.get(groupId);
        if (subLayer == null) {
            subLayer = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            _group2Listener.put(groupId, subLayer);
        }
        subLayer.put(generateNewID, 1);
    }

    public synchronized void registerListenerAll(long groupId, long universe, long[] objects, KEventMultiListener listener) {
        long generateNewID = _internalListenerKeyGen.nextKey();
        _multiListener.put(generateNewID, listener);
        _listener2Objects.put(generateNewID, objects);
        ArrayLongLongMap subLayer;
        for (int i = 0; i < objects.length; i++) {
            subLayer = _obj2Listener.get(objects[i]);
            if (subLayer == null) {
                subLayer = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                _obj2Listener.put(objects[i], subLayer);
            }
            subLayer.put(generateNewID, universe);
        }
        subLayer = _group2Listener.get(groupId);
        if (subLayer == null) {
            subLayer = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            _group2Listener.put(groupId, subLayer);
        }
        subLayer.put(generateNewID, 2);
    }

    public synchronized void unregister(long groupId) {
        ArrayLongLongMap groupLayer = _group2Listener.get(groupId);
        if (groupLayer != null) {
            groupLayer.each(new KLongLongMapCallBack() {
                @Override
                public void on(long listenerID, long value) {
                    if (value == 1) {
                        _simpleListener.remove(listenerID);
                        long previousObject = _listener2Object.get(listenerID);
                        _listener2Object.remove(listenerID);
                        ArrayLongLongMap _obj2ListenerLayer = _obj2Listener.get(previousObject);
                        if (_obj2ListenerLayer != null) {
                            _obj2ListenerLayer.remove(listenerID);
                        }
                    } else {
                        _multiListener.remove(listenerID);
                        long[] previousObjects = _listener2Objects.get(listenerID);
                        for (int i = 0; i < previousObjects.length; i++) {
                            ArrayLongLongMap _obj2ListenerLayer = _obj2Listener.get(previousObjects[i]);
                            if (_obj2ListenerLayer != null) {
                                _obj2ListenerLayer.remove(listenerID);
                            }
                        }
                        _listener2Objects.remove(listenerID);
                    }
                }
            });
            _group2Listener.remove(groupId);
        }
    }

    public void clear() {
        _simpleListener.clear();
        _multiListener.clear();
        _obj2Listener.clear();
        _group2Listener.clear();
        _listener2Object.clear();
        _listener2Objects.clear();
    }

    public void setManager(KMemoryManager manager) {
        this._manager = manager;
    }

    public void dispatch(final KMessage param) {
        if (_manager != null) {
            ArrayLongMap<KUniverse> _cacheUniverse = new ArrayLongMap<KUniverse>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            if (param instanceof Events) {
                Events messages = (Events) param;
                KContentKey[] toLoad = new KContentKey[messages.size()];
                final ArrayLongLongMap[] multiCounters = new ArrayLongLongMap[1];
                //first step, we filter and select relevant keys
                for (int i = 0; i < messages.size(); i++) {
                    KContentKey loopKey = messages.getKey(i);
                    ArrayLongLongMap listeners = _obj2Listener.get(loopKey.obj);

                    final boolean[] isSelect = {false};
                    if (listeners != null) {
                        listeners.each(new KLongLongMapCallBack() {
                            @Override
                            public void on(long listenerKey, long universeKey) {
                                if (universeKey == loopKey.universe) {
                                    isSelect[0] = true;
                                    if (_multiListener.contains(listenerKey)) {
                                        if (multiCounters[0] == null) {
                                            multiCounters[0] = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                                        }
                                        long previous = 0;
                                        if (multiCounters[0].contains(listenerKey)) {
                                            previous = multiCounters[0].get(listenerKey);
                                        }
                                        previous++;
                                        multiCounters[0].put(listenerKey, previous);
                                    }
                                }
                            }
                        });
                    }
                    if (isSelect[0]) {
                        toLoad[i] = loopKey;
                    }
                }
                ((HeapMemoryManager) _manager).bumpKeysToCache(toLoad, new KCallback<KMemoryElement[]>() {
                    @Override
                    public void on(KMemoryElement[] kMemoryElements) {
                        final ArrayLongMap<KObject[]>[] multiObjectSets = new ArrayLongMap[1];
                        final ArrayLongLongMap[] multiObjectIndexes = new ArrayLongLongMap[1];
                        if (multiCounters[0] != null) {
                            multiObjectSets[0] = new ArrayLongMap<KObject[]>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                            multiObjectIndexes[0] = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                            //init next result
                            multiCounters[0].each(new KLongLongMapCallBack() {
                                @Override
                                public void on(long listenerKey, long value) {
                                    multiObjectSets[0].put(listenerKey, new KObject[(int) value]);
                                    multiObjectIndexes[0].put(listenerKey, 0);
                                }
                            });
                        }
                        //first we try to select unary listener
                        ArrayLongLongMap listeners;
                        for (int i = 0; i < messages.size(); i++) {
                            if (kMemoryElements[i] != null && kMemoryElements[i] instanceof HeapMemorySegment) {
                                KContentKey correspondingKey = toLoad[i];
                                listeners = _obj2Listener.get(correspondingKey.obj);
                                if (listeners != null) {
                                    KUniverse cachedUniverse = _cacheUniverse.get(correspondingKey.universe);
                                    if (cachedUniverse == null) {
                                        cachedUniverse = _manager.model().universe(correspondingKey.universe);
                                        _cacheUniverse.put(correspondingKey.universe, cachedUniverse);
                                    }
                                    HeapMemorySegment segment = (HeapMemorySegment) kMemoryElements[i];
                                    KObject toDispatch = ((AbstractKModel) _manager.model()).createProxy(correspondingKey.universe, correspondingKey.time, correspondingKey.obj, _manager.model().metaModel().metaClasses()[segment.metaClassIndex()]);
                                    if (toDispatch != null) {
                                        kMemoryElements[i].inc();
                                    }
                                    KMeta[] meta = new KMeta[messages.getIndexes(i).length];
                                    for (int j = 0; j < messages.getIndexes(i).length; j++) {
                                        meta[j] = toDispatch.metaClass().meta(messages.getIndexes(i)[j]);
                                    }
                                    listeners.each(new KLongLongMapCallBack() {
                                        @Override
                                        public void on(long listenerKey, long value) {
                                            KEventListener listener = _simpleListener.get(listenerKey);
                                            if (listener != null) {
                                                listener.on(toDispatch, meta);
                                            } else {
                                                KEventMultiListener multiListener = _multiListener.get(listenerKey);
                                                if (multiListener != null) {
                                                    if (multiObjectSets[0] != null && multiObjectIndexes[0] != null) {
                                                        long index = multiObjectIndexes[0].get(listenerKey);
                                                        multiObjectSets[0].get(listenerKey)[(int) index] = toDispatch;
                                                        index = index + 1;
                                                        multiObjectIndexes[0].put(listenerKey, index);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        if (multiObjectSets[0] != null) {
                            multiObjectSets[0].each(new KLongMapCallBack<KObject[]>() {
                                @Override
                                public void on(long key, KObject[] value) {
                                    KEventMultiListener multiListener = _multiListener.get(key);
                                    if (multiListener != null) {
                                        multiListener.on(value);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

}
