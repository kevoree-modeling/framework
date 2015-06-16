
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;

/**
 * @native ts
 * private _isDirty = false;
 * constructor(initalCapacity: number, loadFactor : number) { }
 * public clear():void { for(var p in this){ this._isDirty=true;if(this.hasOwnProperty(p) && p.indexOf('_') != 0){ delete this[p];}} }
 * public get(key:number):number { return this[key]; }
 * public put(key:number, pval : number):void { this._isDirty=true; this[key] = pval;}
 * public contains(key:number):boolean { return this.hasOwnProperty(<any>key);}
 * public remove(key:number):number { var tmp = this[key]; delete this[key]; return tmp; }
 * public size():number { return Object.keys(this).length-1; }
 * public each(callback: (p : number, p1 : number) => void): void { for(var p in this){ if(this.hasOwnProperty(p) && p.indexOf('_') != 0){ callback(<number>p,this[p]); } } }
 * public isDirty():boolean { return this._isDirty; }
 * public setClean(mm):void { this._isDirty = false; }
 * public setDirty():void { this._isDirty = true; }
 */
public class ArrayLongLongMap implements KLongLongMap {

    protected int elementCount;

    protected Entry[] elementData;

    protected int elementDataSize;

    protected int threshold;

    private final int initalCapacity;

    private final float loadFactor;

    protected boolean _isDirty = false;

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

    public ArrayLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this.initalCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;
        elementCount = 0;
        elementData = new Entry[initalCapacity];
        elementDataSize = initalCapacity;
        computeMaxSize();
    }

    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            this.elementData = new Entry[initalCapacity];
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
            if (++elementCount > threshold) {
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



