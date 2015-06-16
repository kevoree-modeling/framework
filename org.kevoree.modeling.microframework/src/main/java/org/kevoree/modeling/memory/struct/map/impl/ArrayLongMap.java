
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.KLongMap;
import org.kevoree.modeling.memory.struct.map.KLongMapCallBack;

/**
 * @native ts
 * constructor(initalCapacity: number, loadFactor : number) { }
 * public clear():void { for(var p in this){if(this.hasOwnProperty(p)){delete this[p];} } }
 * public get(key:number):V { return this[key]; }
 * public put(key:number, pval : V):V { var previousVal = this[key];this[key] = pval;return previousVal;}
 * public contains(key:number):boolean { return this.hasOwnProperty(<any>key);}
 * public remove(key:number):V { var tmp = this[key]; delete this[key]; return tmp; }
 * public size():number { return Object.keys(this).length; }
 * public each(callback: (p : number, p1 : V) => void): void { for(var p in this){ if(this.hasOwnProperty(p)){ callback(<number>p,this[p]); } } }
 */
public class ArrayLongMap<V> implements KLongMap<V> {

    protected int elementCount;

    protected Entry<V>[] elementData;

    private int elementDataSize;

    protected int threshold;

    private final int initalCapacity;

    private final float loadFactor;

    /**
     * @ignore ts
     */
    static final class Entry<V> {
        Entry<V> next;
        long key;
        V value;

        Entry(long theKey, V theValue) {
            this.key = theKey;
            this.value = theValue;
        }
    }

    @SuppressWarnings("unchecked")
    Entry<V>[] newElementArray(int s) {
        return new Entry[s];
    }

    public ArrayLongMap(int p_initalCapacity, float p_loadFactor) {
        this.initalCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;
        elementCount = 0;
        elementData = newElementArray(initalCapacity);
        elementDataSize = initalCapacity;
        computeMaxSize();
    }

    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            this.elementData = newElementArray(initalCapacity);
            this.elementDataSize = initalCapacity;
        }
    }

    private void computeMaxSize() {
        threshold = (int) (elementDataSize * loadFactor);
    }

    @Override
    public boolean contains(long key) {
        if (elementDataSize == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        Entry<V> m = findNonNullKeyEntry(key, index);
        return m != null;
    }

    @Override
    public V get(long key) {
        if (elementDataSize == 0) {
            return null;
        }
        Entry<V> m;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        m = findNonNullKeyEntry(key, index);
        if (m != null) {
            return m.value;
        }
        return null;
    }

    final Entry<V> findNonNullKeyEntry(long key, int index) {
        Entry<V> m = elementData[index];
        while (m != null) {
            if (key == m.key) {
                return m;
            }
            m = m.next;
        }
        return null;
    }

    @Override
    public void each(KLongMapCallBack<V> callback) {
        for (int i = 0; i < elementDataSize; i++) {
            if (elementData[i] != null) {
                Entry<V> current = elementData[i];
                callback.on(elementData[i].key, elementData[i].value);
                while (current.next != null) {
                    current = current.next;
                    callback.on(current.key, current.value);
                }
            }
        }
    }

    @Override
    public void put(long key, V value) {
        Entry<V> entry = null;
        int hash = (int) (key);
        int index = -1;
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }
        if (entry == null) {
            if (++elementCount > threshold) {
                rehash();
                index = (hash & 0x7FFFFFFF) % elementDataSize;
            }
            entry = createHashedEntry(key, index);
        }
        entry.value = value;
    }

    Entry<V> createHashedEntry(long key, int index) {
        Entry<V> entry = new Entry<V>(key, null);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        Entry<V>[] newData = newElementArray(length);
        for (int i = 0; i < elementDataSize; i++) {
            Entry<V> entry = elementData[i];
            while (entry != null) {
                int index = ((int) entry.key & 0x7FFFFFFF) % length;
                Entry<V> next = entry.next;
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

    public V remove(long key) {
        Entry<V> entry = removeEntry(key);
        if (entry == null) {
            return null;
        } else {
            return entry.value;
        }
    }

    Entry<V> removeEntry(long key) {
        if (elementDataSize == 0) {
            return null;
        }
        Entry<V> entry;
        Entry<V> last = null;
        int hash = (int) key;
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        entry = elementData[index];
        while (entry != null && !(/*((int)segment.key) == hash &&*/ key == entry.key)) {
            last = entry;
            entry = entry.next;
        }
        if (entry == null) {
            return null;
        }
        if (last == null) {
            elementData[index] = entry.next;
        } else {
            last.next = entry.next;
        }
        elementCount--;
        return entry;
    }

    public int size() {
        return elementCount;
    }

}



