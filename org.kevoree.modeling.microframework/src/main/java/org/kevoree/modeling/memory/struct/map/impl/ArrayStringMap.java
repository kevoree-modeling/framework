
package org.kevoree.modeling.memory.struct.map.impl;

/* From an original idea https://code.google.com/p/jdbm2/ */

import org.kevoree.modeling.memory.struct.map.KStringMap;
import org.kevoree.modeling.memory.struct.map.KStringMapCallBack;

/**
 * @native ts
 * constructor(initalCapacity: number, loadFactor : number) { }
 * public clear():void { for(var p in this){ if(this.hasOwnProperty(p)){ delete this[p];} } }
 * public get(key:string):V { return this[key]; }
 * public put(key:string, pval : V):V { var previousVal = this[key];this[key] = pval;return previousVal;}
 * public contains(key:string):boolean { return this.hasOwnProperty(key);}
 * public remove(key:string):V { var tmp = this[key]; delete this[key]; return tmp; }
 * public size():number { return Object.keys(this).length; }
 * public each(callback: (p : string, p1 : V) => void): void { for(var p in this){ if(this.hasOwnProperty(p)){ callback(<string>p,this[p]); } } }
 */
public class ArrayStringMap<V> implements KStringMap<V> {

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
        String key;
        V value;

        Entry(String theKey, V theValue) {
            this.key = theKey;
            this.value = theValue;
        }
    }

    @SuppressWarnings("unchecked")
    Entry<V>[] newElementArray(int s) {
        return new Entry[s];
    }

    public ArrayStringMap(int p_initalCapacity, float p_loadFactor) {
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
    public boolean contains(String key) {
        if (elementDataSize == 0) {
            return false;
        }
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        Entry<V> m = findNonNullKeyEntry(key, index);
        return m != null;
    }

    @Override
    public V get(String key) {
        if (key == null || elementDataSize == 0) {
            return null;
        }
        Entry<V> m;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        m = findNonNullKeyEntry(key, index);
        if (m != null) {
            return m.value;
        }
        return null;
    }

    final Entry<V> findNonNullKeyEntry(String key, int index) {
        Entry<V> m = elementData[index];
        while (m != null) {
            if (key.equals(m.key)) {
                return m;
            }
            m = m.next;
        }
        return null;
    }

    @Override
    public void each(KStringMapCallBack<V> callback) {
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
    public void put(String key, V value) {
        if (key == null) {
            return;
        }
        Entry<V> entry = null;
        int index = -1;
        int hash = key.hashCode();
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

    Entry<V> createHashedEntry(String key, int index) {
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
                int hash = entry.key.hashCode();
                int index = (hash & 0x7FFFFFFF) % length;
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

    @Override
    public void remove(String key) {
        if (elementDataSize == 0) {
            return;
        }
        Entry<V> entry;
        Entry<V> last = null;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        entry = elementData[index];
        while (entry != null && !(key.equals(entry.key))) {
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



