
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;

import java.util.HashMap;
import java.util.Random;

/**
 * @native ts
 * private _isDirty = false;
 * constructor(initialCapacity: number, loadFactor : number) { }
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

    protected volatile int elementCount;

    protected volatile int elementDataSize;

    protected int threshold;

    protected volatile long[] elementKV;

    protected volatile int[] elementNext;

    protected int[] elementHash;

    private final int initialCapacity;

    private final float loadFactor;

    protected boolean _isDirty = false;

    public ArrayLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this.initialCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;
        this.elementCount = 0;
        this.elementKV = new long[initialCapacity * 2];
        this.elementNext = new int[initialCapacity];
        this.elementHash = new int[initialCapacity];
        for (int i = 0; i < initialCapacity; i++) {
            this.elementNext[i] = -1;
            this.elementHash[i] = -1;
        }
        this.elementDataSize = initialCapacity;
        this.threshold = (int) (elementDataSize * loadFactor);
    }

    public void clear() {
        if (elementCount > 0) {
            this.elementCount = 0;
            this.elementKV = new long[initialCapacity * 2];
            this.elementNext = new int[initialCapacity];
            this.elementHash = new int[initialCapacity];
            for (int i = 0; i < initialCapacity; i++) {
                this.elementNext[i] = -1;
                this.elementHash[i] = -1;
            }
            this.elementDataSize = initialCapacity;
            this.threshold = (int) (elementDataSize * loadFactor);
        }
    }

    void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        long[] newElementKV = new long[length * 2];
        System.arraycopy(this.elementKV, 0, newElementKV, 0, this.elementKV.length);
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < this.elementNext.length; i++) {
            if (this.elementNext[i] != -1) { //there is a real value
                int index = ((int) this.elementKV[i * 2] & 0x7FFFFFFF) % length;
                int currentHashedIndex = newElementHash[index];
                if (currentHashedIndex != -1) {
                    newElementNext[i] = currentHashedIndex;
                } else {
                    newElementNext[i] = -2; //special char to tag used values
                }
                newElementHash[index] = i;
            }
        }
        //set value for all
        this.elementKV = newElementKV;
        this.elementHash = newElementHash;
        this.elementNext = newElementNext;
        this.elementDataSize = length;
        this.threshold = (int) (elementDataSize * loadFactor);
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        for (int i = 0; i < this.elementNext.length; i++) {
            if (this.elementNext[i] != -1) { //there is a real value
                callback.on(this.elementKV[i * 2], this.elementKV[i * 2 + 1]);
            }
        }
    }

    @Override
    public boolean contains(long key) {
        if (elementDataSize == 0) {
            return false;
        }
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        int m = findNonNullKeyEntry(key, index);
        return m != -1;
    }

    @Override
    public long get(long key) {
        if (elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        int m;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % elementDataSize;
        m = findNonNullKeyEntry(key, index);
        if (m != -1) {
            return this.elementKV[(m * 2) + 1] /* getValue */;
        }
        return KConfig.NULL_LONG;
    }

    final int findNonNullKeyEntry(long key, int index) {
        int m = this.elementHash[index];
        while (m >= 0) {
            if (key == this.elementKV[m * 2] /* getKey */) {
                return m;
            }
            m = this.elementNext[m];
        }
        return -1;
    }

    @Override
    public synchronized void put(long key, long value) {
        this._isDirty = true;
        int entry = -1;
        int index = -1;
        int hash = (int) (key);
        if (elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(elementDataSize);
                index = (hash & 0x7FFFFFFF) % elementDataSize;
            }
            int newIndex = (this.elementCount - 1);
            this.elementKV[newIndex * 2] = key;
            this.elementKV[newIndex * 2 + 1] = value;
            int currentHashedIndex = this.elementHash[index];
            if (currentHashedIndex != -1) {
                this.elementNext[newIndex] = currentHashedIndex;
            } else {
                this.elementNext[newIndex] = -2; //special char to tag used values
            }
            this.elementHash[index] = newIndex;
        } else {
            this.elementKV[entry + 1] = value;/*setValue*/
        }
    }

    public void remove(long key) {
        /*
        if (elementDataSize == 0) {
            return;
        }
        int index;
        int entry;
        int last = -1;
        int hash = (int) (key);
        index = (hash & 0x7FFFFFFF) % elementDataSize;
        entry = this.elementHash[index];
        while (entry != -1 && !(key == entry.key)) {
            last = entry;
            entry = this.elementNext[entry];
        }
        if (entry == -1) {
            return;
        }
        if (last == -1) {


            elementData[index] = entry.next;
        } else {
            this.elementNext[]
            this.elementHash[]

            last.next = entry.next;
        }
        elementCount--;
        */
    }

    public int size() {
        return this.elementCount;
    }


    public static void main(String[] args) {

        /*
        HashMap<Long, Long> op_map_ref = new HashMap<Long, Long>();
        ArrayLongLongMap op_map = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        int nb = 1000000;
        Random random = new Random();
        for (long i = 0; i < nb; i++) {
            long key = random.nextLong();
            long value = random.nextLong();
            op_map.put(key, value);
            op_map_ref.put(key, value);
        }

        for (Long key : op_map_ref.keySet()) {
            long resolved = op_map.get(key);
            long resolved2 = op_map_ref.get(key);
            if (resolved != resolved2) {
                throw new RuntimeException("WTF " + resolved2 + "-" + resolved);
            }
        }
*/



        long time = 0;
        for (int j = 0; j < 30; j++) {
            long before = System.currentTimeMillis();
            //HashMap<Long, Long> op_map = new HashMap<Long, Long>();
            ArrayLongLongMap op_map = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            int nb = 1000000;
            for (long i = 0; i < nb; i++) {
                op_map.put(i, i);
            }
            for (long i = 0; i < nb; i++) {
                long resolved = op_map.get(i);
                if (resolved != i) {
                    throw new RuntimeException("WTF " + i + "-" + resolved);
                }
            }
            if (j >= 10) {
                time += System.currentTimeMillis() - before;
            }
        }
        System.err.println((time / 20) + "ms");



    }

}



