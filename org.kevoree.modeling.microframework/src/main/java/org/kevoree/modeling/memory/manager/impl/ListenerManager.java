package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap;

public class ListenerManager {

    private KMemoryManager _manager;

    private KeyCalculator _keyGen;

    public ArrayLongMap<HeapListener> _listeners;

    public ArrayLongMap<long[]> _listener2Objects;

    public ArrayLongMap<long[]> _obj2Listener;

    public ListenerManager() {
        _keyGen = new KeyCalculator((short) 0, 0);
        _listeners = new ArrayLongMap<HeapListener>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _obj2Listener = new ArrayLongMap<long[]>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        _listener2Objects = new ArrayLongMap<long[]>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    public void clear() {
        _listeners.clear();
        _obj2Listener.clear();
        _listener2Objects.clear();
    }

    public KListener createListener(long p_universe) {
        HeapListener newListener = new HeapListener(p_universe, this, _keyGen.nextKey());
        _listeners.put(newListener.listenerID(), newListener);
        return newListener;
    }

    public synchronized void manageRegistration(long listenerID, KObject origin) {
        if (origin != null) {
            //register
            //L2O
            if (!_listener2Objects.contains(listenerID)) {
                long[] newRec = new long[1];
                newRec[0] = origin.uuid();
                _listener2Objects.put(listenerID, newRec);
            } else {
                long[] previous = _listener2Objects.get(listenerID);
                long[] newArrayRec = new long[previous.length + 1];
                System.arraycopy(previous, 0, newArrayRec, 0, previous.length);
                newArrayRec[previous.length] = origin.uuid();
                _listener2Objects.put(listenerID, newArrayRec);
            }
            //O2L
            if (!_obj2Listener.contains(origin.uuid())) {
                long[] newRec = new long[1];
                newRec[0] = listenerID;
                _listener2Objects.put(origin.uuid(), newRec);
            } else {
                long[] previous = _obj2Listener.get(origin.uuid());
                long[] newArrayRec = new long[previous.length + 1];
                System.arraycopy(previous, 0, newArrayRec, 0, previous.length);
                newArrayRec[previous.length] = listenerID;
                _listener2Objects.put(origin.uuid(), newArrayRec);
            }
        } else {
            //unregisterAll
            long[] objs = _listener2Objects.get(listenerID);
            if (objs != null) {
                _listener2Objects.remove(listenerID);
                for (int i = 0; i < objs.length; i++) {
                    long[] registeredListener = _obj2Listener.get(objs[i]);
                    int foundIndex = -1;
                    for (int j = 0; j < registeredListener.length; j++) {
                        if (objs[j] == origin.uuid()) {
                            foundIndex = j;
                        }
                    }
                    if (foundIndex != -1) {
                        if (foundIndex == 0) {
                            long[] registeredListener2 = new long[registeredListener.length - 1];
                            System.arraycopy(objs, foundIndex, registeredListener2, 0, registeredListener.length - 1);
                            _obj2Listener.put(objs[i], registeredListener2);
                        } else {
                            long[] registeredListener2 = new long[registeredListener.length - 1];
                            System.arraycopy(objs, 0, registeredListener2, 0, foundIndex - 1);
                            System.arraycopy(objs, foundIndex + 1, registeredListener2, foundIndex, registeredListener.length - foundIndex - 1);
                            _obj2Listener.put(objs[i], registeredListener2);
                        }
                    }
                }
            }
        }
    }

    public void setManager(KMemoryManager manager) {
        this._manager = manager;
    }

    public boolean isListened(KContentKey key) {
        long[] notifier = _obj2Listener.get(key.obj);
        if (notifier != null && notifier.length > 0) {
            return true;
        }
        return false;
    }


}

