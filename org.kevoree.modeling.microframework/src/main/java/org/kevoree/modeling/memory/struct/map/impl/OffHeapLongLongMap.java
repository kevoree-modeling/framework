
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;

/**
 * @ignore ts
 *
 * - memory structure:
 * - root      | elem count (4) | elem data size (4) | dirty (1) | elem data (size * 8) |
 * - entry     | key (8)        | value (8)          |
 */
public class OffHeapLongLongMap implements KLongLongMap {

    protected int elementCount;
    protected int elementDataSize;
    protected boolean _isDirty = false;

    protected Entry[] elementData;

    protected int _threshold;
    private final int _initalCapacity;
    private final float _loadFactor;


    /**
     * @ignore ts
     */
    static final class Entry {
        Entry next;
        long key;
        long value;

        Entry(long theKey, long theValue) {
            this.key = theKey;
            this.value = theValue;
        }
    }

    public OffHeapLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this._initalCapacity = p_initalCapacity;
        this._loadFactor = p_loadFactor;
        elementCount = 0;
        elementData = new Entry[_initalCapacity];
        elementDataSize = _initalCapacity;
        computeMaxSize();
    }

    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            this.elementData = new Entry[_initalCapacity];
            this.elementDataSize = _initalCapacity;
        }
    }

    private void computeMaxSize() {
        _threshold = (int) (elementDataSize * _loadFactor);
    }

    @Override
    public boolean contains(long key) {
        if (elementDataSize == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        Entry m = findNonNullKeyEntry(key, index);
        return m != null;
    }

    @Override
    public long get(long key) {
        if (elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        Entry m;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        m = findNonNullKeyEntry(key, index);
        if (m != null) {
            return m.value;
        }
        return KConfig.NULL_LONG;
    }

    final Entry findNonNullKeyEntry(long key, int index) {
        Entry m = elementData[index];
        while (m != null) {
            if (key == m.key) {
                return m;
            }
            m = m.next;
        }
        return null;
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        for (int i = 0; i < elementDataSize; i++) {
            if (elementData[i] != null) {
                Entry current = elementData[i];
                callback.on(elementData[i].key, elementData[i].value);
                while (current.next != null) {
                    current = current.next;
                    callback.on(current.key, current.value);
                }
            }
        }
    }

    @Override
    public synchronized void put(long key, long value) {
        _isDirty = true;
        Entry entry = null;
        int index = -1;
        int hash = (int) (key);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }
        if (entry == null) {
            if (++elementCount > _threshold) {
                rehash();
                index = (hash & 0x7FFFFFFF) % elementDataSize;
            }
            entry = createHashedEntry(key, index);
        }
        entry.value = value;
    }

    Entry createHashedEntry(long key, int index) {
        Entry entry = new Entry(key, KConfig.NULL_LONG);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        Entry[] newData = new Entry[length];
        for (int i = 0; i < elementDataSize; i++) {
            Entry entry = elementData[i];
            while (entry != null) {
                int index = ((int) entry.key & 0x7FFFFFFF) % length;
                Entry next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        elementDataSize = length;
        computeMaxSize();
    }

    void rehash() {
        rehashCapacity(elementDataSize);
    }

    public void remove(long key) {
        if (elementDataSize == 0) {
            return;
        }
        int index = 0;
        Entry entry;
        Entry last = null;
        int hash = (int) (key);
        index = (hash & 0x7FFFFFFF) % elementDataSize;
        entry = elementData[index];
        while (entry != null && !(/*((int)segment.key) == hash &&*/ key == entry.key)) {
            last = entry;
            entry = entry.next;
        }
        if (entry == null) {
            return;
        }
        if (last == null) {
            elementData[index] = entry.next;
        } else {
            last.next = entry.next;
        }
        elementCount--;
    }

    public int size() {
        return elementCount;
    }

}



