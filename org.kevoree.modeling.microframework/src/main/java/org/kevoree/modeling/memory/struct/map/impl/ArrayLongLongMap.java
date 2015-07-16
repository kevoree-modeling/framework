
package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;

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

    protected volatile InternalState state = null;

    protected int threshold;

    private final int initialCapacity;

    private final float loadFactor;

    protected boolean _isDirty = false;

    /** @native ts
     * */
    class InternalState {

        public final int elementDataSize;

        public final long[] elementKV;

        public final int[] elementNext;

        public final int[] elementHash;

        public InternalState(int elementDataSize, long[] elementKV, int[] elementNext, int[] elementHash) {
            this.elementDataSize = elementDataSize;
            this.elementKV = elementKV;
            this.elementNext = elementNext;
            this.elementHash = elementHash;
        }
    }

    public ArrayLongLongMap(int p_initalCapacity, float p_loadFactor) {
        this.initialCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;
        this.elementCount = 0;
        InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity]);
        for (int i = 0; i < initialCapacity; i++) {
            newstate.elementNext[i] = -1;
            newstate.elementHash[i] = -1;
        }
        this.state = newstate;
        this.threshold = (int) (state.elementDataSize * loadFactor);
    }

    public void clear() {
        if (elementCount > 0) {
            this.elementCount = 0;
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity]);
            for (int i = 0; i < initialCapacity; i++) {
                newstate.elementNext[i] = -1;
                newstate.elementHash[i] = -1;
            }
            this.state = newstate;
            this.threshold = (int) (state.elementDataSize * loadFactor);
        }
    }

    void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        long[] newElementKV = new long[length * 2];
        System.arraycopy(state.elementKV, 0, newElementKV, 0, state.elementKV.length);
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < state.elementNext.length; i++) {
            if (state.elementNext[i] != -1) { //there is a real value
                int index = ((int) state.elementKV[i * 2] & 0x7FFFFFFF) % length;
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
        state = new InternalState(length, newElementKV, newElementNext, newElementHash);
        this.threshold = (int) (length * loadFactor);
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        InternalState internalState = state;
        for (int i = 0; i < internalState.elementNext.length; i++) {
            if (internalState.elementNext[i] != -1) { //there is a real value
                callback.on(internalState.elementKV[i * 2], internalState.elementKV[i * 2 + 1]);
            }
        }
    }

    @Override
    public boolean contains(long key) {
        if (state.elementDataSize == 0) {
            return false;
        }
        InternalState internalState = state;
        int hash = (int) (key);
        int index = (hash & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (key == internalState.elementKV[m * 2] /* getKey */) {
                return m != -1;
            }
            m = internalState.elementNext[m];
        }
        return m != -1;
    }

    @Override
    public long get(long key) {
        if (state.elementDataSize == 0) {
            return KConfig.NULL_LONG;
        }
        InternalState internalState = state;
        int index = ((int) (key) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (key == internalState.elementKV[m * 2] /* getKey */) {
                return internalState.elementKV[(m * 2) + 1]; /* getValue */
            } else {
                m = internalState.elementNext[m];
            }
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public synchronized void put(long key, long value) {
        this._isDirty = true;
        int entry = -1;
        int index = -1;
        int hash = (int) (key);
        if (state.elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % state.elementDataSize;
            entry = findNonNullKeyEntry(key, index);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(state.elementDataSize);
                index = (hash & 0x7FFFFFFF) % state.elementDataSize;
            }
            int newIndex = (this.elementCount - 1);
            state.elementKV[newIndex * 2] = key;
            state.elementKV[newIndex * 2 + 1] = value;
            int currentHashedIndex = state.elementHash[index];
            if (currentHashedIndex != -1) {
                state.elementNext[newIndex] = currentHashedIndex;
            } else {
                state.elementNext[newIndex] = -2; //special char to tag used values
            }
            //now the object is reachable to other thread everything should be ready
            state.elementHash[index] = newIndex;
        } else {
            state.elementKV[entry + 1] = value;/*setValue*/
        }
    }

    final int findNonNullKeyEntry(long key, int index) {
        int m = state.elementHash[index];
        while (m >= 0) {
            if (key == state.elementKV[m * 2] /* getKey */) {
                return m;
            }
            m = state.elementNext[m];
        }
        return -1;
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



/*
    public static void main(String[] args) {


        HashMap<Long, Long> op_map_ref = new HashMap<Long, Long>();
        ArrayLongLongMap op_map = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        int nb = 1000000;
        Random random = new Random();

        ExecutorService service = Executors.newFixedThreadPool(1000);

        for (long i = 0; i < nb; i++) {
            final long key = random.nextLong();
            final long value = random.nextLong();
            op_map_ref.put(key, value);

            final long finalI = i;
            service.submit(new Runnable() {
                @Override
                public void run() {

                    //System.err.println(finalI);

                    try {
                        op_map.put(key, value);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

        }

        service.shutdown();
        try {
            service.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (op_map_ref.size() != op_map.size()) {
            throw new RuntimeException("WTF SIZE != " + op_map_ref.size() + "-" + op_map.size());
        }

        for (Long key : op_map_ref.keySet()) {
            long resolved = op_map.get(key);
            long resolved2 = op_map_ref.get(key);
            if (resolved != resolved2) {
                throw new RuntimeException("WTF " + resolved2 + "-" + resolved);
            }
        }




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



    }*/

}



