
package org.kevoree.modeling.memory.struct.cache.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.manager.impl.ResolutionHelper;
import org.kevoree.modeling.memory.struct.cache.KCache;
import org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment;
import org.kevoree.modeling.meta.KMetaModel;

public class ArrayMemoryCache implements KCache {

    protected volatile int elementCount;

    protected volatile int droppedCount;

    protected volatile InternalState state = null;

    protected int threshold;

    protected boolean _isDirty = false;

    private final float loadFactor;

    class InternalState {

        public final int elementDataSize;

        public final long[] elementK3;

        public final int[] elementNext;

        public final int[] elementHash;

        public final KMemoryElement[] values;

        public InternalState(int p_elementDataSize, long[] p_elementKE, int[] p_elementNext, int[] p_elementHash, KMemoryElement[] p_values) {
            this.elementDataSize = p_elementDataSize;
            this.elementK3 = p_elementKE;
            this.elementNext = p_elementNext;
            this.elementHash = p_elementHash;
            this.values = p_values;
        }
    }

    /**
     * @ignore ts
     */
    private KObjectWeakReference rootReference = null;

    public ArrayMemoryCache() {
        int initialCapacity = KConfig.CACHE_INIT_SIZE;
        this.loadFactor = KConfig.CACHE_LOAD_FACTOR;
        this.elementCount = 0;
        this.droppedCount = 0;
        InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 3], new int[initialCapacity], new int[initialCapacity], new KMemoryElement[initialCapacity]);
        for (int i = 0; i < initialCapacity; i++) {
            newstate.elementNext[i] = -1;
            newstate.elementHash[i] = -1;
        }
        this.state = newstate;
        this.threshold = (int) (state.elementDataSize * loadFactor);
    }

    protected final void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);

        long[] newElementKV = new long[length * 3];
        System.arraycopy(state.elementK3, 0, newElementKV, 0, state.elementDataSize * 3);

        KMemoryElement[] newValues = new KMemoryElement[length];
        System.arraycopy(state.values, 0, newValues, 0, state.elementDataSize);

        int[] newElementNext = new int[length];

        int[] newElementHash = new int[length];

        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < state.elementDataSize; i++) {
            if (state.values[i] != null) { //there is a real value
                int hash = (int) (state.elementK3[(i * 3)] ^ state.elementK3[(i * 3) + 1] ^ state.elementK3[(i * 3) + 2]);
                int index = (hash & 0x7FFFFFFF) % length;
                newElementNext[i] = newElementHash[index];
                newElementHash[index] = i;
            }
        }
        //set value for all
        state = new InternalState(length, newElementKV, newElementNext, newElementHash, newValues);
        this.threshold = (int) (length * loadFactor);
    }

    @Override
    public KMemoryElement get(long universe, long time, long obj) {
        InternalState internalState = state;
        if (internalState.elementDataSize == 0) {
            return null;
        }
        int index = (((int) (universe ^ time ^ obj)) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (universe == internalState.elementK3[m * 3] && time == internalState.elementK3[(m * 3) + 1] && obj == internalState.elementK3[(m * 3) + 2]) {
                return internalState.values[m]; /* getValue */
            } else {
                m = internalState.elementNext[m];
            }
        }
        return null;
    }

    @Override
    public void putAndReplace(long universe, long time, long obj, KMemoryElement payload) {
        internal_put(universe, time, obj, payload, true);
    }

    @Override
    public KMemoryElement getOrPut(long universe, long time, long obj, KMemoryElement payload) {
        return internal_put(universe, time, obj, payload, false);
    }

    private final KMemoryElement internal_put(long universe, long time, long obj, KMemoryElement payload, boolean force) {
        this._isDirty = true;
        int entry = -1;
        int index = -1;
        int hash = (int) (universe ^ time ^ obj);
        if (state.elementDataSize != 0) {
            index = (hash & 0x7FFFFFFF) % state.elementDataSize;
            entry = findNonNullKeyEntry(universe, time, obj, index, state);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(state.elementDataSize);
                index = (hash & 0x7FFFFFFF) % state.elementDataSize;
            }
            int newIndex = (this.elementCount - 1 + this.droppedCount);
            state.elementK3[newIndex * 3] = universe;
            state.elementK3[(newIndex * 3) + 1] = time;
            state.elementK3[(newIndex * 3) + 2] = obj;
            state.values[newIndex] = payload;
            state.elementNext[newIndex] = state.elementHash[index];
            state.elementHash[index] = newIndex;
            //now the object is reachable to other thread everything should be ready
            state.elementHash[index] = newIndex;
            return payload;
        } else {
            if (force) {
                state.values[entry] = payload;/*setValue*/
                return payload;
            } else {
                return state.values[entry];
            }
        }
    }

    final int findNonNullKeyEntry(long universe, long time, long obj, int index, InternalState internalState) {
        int m = state.elementHash[index];
        while (m >= 0) {
            if (universe == internalState.elementK3[m * 3] && time == internalState.elementK3[(m * 3) + 1] && obj == internalState.elementK3[(m * 3) + 2]) {
                return m;
            }
            m = state.elementNext[m];
        }
        return -1;
    }

    @Override
    public KCacheDirty[] dirties() {
        int nbDirties = 0;
        InternalState internalState = state;
        for (int i = 0; i < internalState.elementDataSize; i++) {
            if (internalState.values[i] != null) {
                if (internalState.values[i].isDirty()) {
                    nbDirties++;
                }
            }
        }
        KCacheDirty[] collectedDirties = new KCacheDirty[nbDirties];
        nbDirties = 0;
        for (int i = 0; i < internalState.elementDataSize; i++) {
            if (internalState.values[i] != null) {
                if (internalState.values[i].isDirty()) {
                    KCacheDirty dirty = new KCacheDirty(new KContentKey(internalState.elementK3[i * 3], internalState.elementK3[(i * 3) + 1], internalState.elementK3[(i * 3) + 2]), internalState.values[i]);
                    collectedDirties[nbDirties] = dirty;
                    nbDirties++;
                }
            }
        }
        return collectedDirties;
    }

    /**
     * @native ts
     */
    @Override
    public void clean(KMetaModel metaModel) {
        common_clean_monitor(null, metaModel);
    }

    /**
     * @native ts
     */
    @Override
    public void monitor(KObject origin) {
        common_clean_monitor(origin, null);
    }

    @Override
    public int size() {
        return this.elementCount;
    }

    private void remove(long universe, long time, long obj, KMetaModel p_metaModel) {
        InternalState internalState = state;
        int hash = (int) (universe ^ time ^ obj);
        int index = (hash & 0x7FFFFFFF) % internalState.elementDataSize;
        if (state.elementDataSize == 0) {
            return;
        }
        int m = state.elementHash[index];
        int last = -1;
        while (m >= 0) {
            if (universe == internalState.elementK3[m * 3] && time == internalState.elementK3[(m * 3) + 1] && obj == internalState.elementK3[(m * 3) + 2]) {
                break;
            }
            last = m;
            m = state.elementNext[m];
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (state.elementNext[m] > 0) {
                state.elementHash[index] = m;
            } else {
                state.elementHash[index] = -1;
            }
        } else {
            state.elementNext[last] = state.elementNext[m];
        }
        state.elementNext[m] = -1;//flag to dropped value
        state.values[m].free(p_metaModel);
        state.values[m] = null;
        this.elementCount--;
        this.droppedCount++;
    }

    /**
     * @ignore ts
     */
    private synchronized void common_clean_monitor(KObject origin, KMetaModel p_metaModel) {
        if (origin != null) {
            if (rootReference != null) {
                rootReference.next = new KObjectWeakReference(origin);
            } else {
                rootReference = new KObjectWeakReference(origin);
            }
        } else {
            KObjectWeakReference current = rootReference;
            KObjectWeakReference previous = null;
            while (current != null) {
                //process current
                if (current.get() == null) {
                    //check is dirty
                    HeapMemorySegment currentEntry = (HeapMemorySegment) this.get(current.universe, current.time, current.uuid);
                    if (currentEntry == null || !currentEntry.isDirty()) {
                        //call the clean sub process for universe/time/uuid
                        MemorySegmentResolutionTrace resolved = ResolutionHelper.resolve_trees(current.universe, current.time, current.uuid, this);
                        resolved.getUniverseTree().dec();
                        if (resolved.getUniverseTree().counter() <= 0) {
                            remove(KConfig.NULL_LONG, KConfig.NULL_LONG, current.uuid, p_metaModel);
                        }
                        resolved.getTimeTree().dec();
                        if (resolved.getTimeTree().counter() <= 0) {
                            remove(resolved.getUniverse(), KConfig.NULL_LONG, current.uuid, p_metaModel);
                        }
                        resolved.getSegment().dec();
                        if (resolved.getSegment().counter() <= 0) {
                            remove(resolved.getUniverse(), resolved.getTime(), current.uuid, p_metaModel);
                        }
                        //change chaining
                        if (previous == null) { //first case
                            rootReference = current.next;
                        } else { //in the middle case
                            previous.next = current.next;
                        }
                    }
                }
                previous = current;
                current = current.next;
            }
        }
    }

    public void clear(KMetaModel metaModel) {
        InternalState internalState = state;
        for (int i = 0; i < internalState.elementDataSize; i++) {
            if (internalState.values[i] != null) {
                internalState.values[i].free(metaModel);
            }
        }
        if (elementCount > 0) {
            int initialCapacity = KConfig.CACHE_INIT_SIZE;
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 3], new int[initialCapacity], new int[initialCapacity], new KMemoryElement[initialCapacity]);
            for (int i = 0; i < initialCapacity; i++) {
                newstate.elementNext[i] = -1;
                newstate.elementHash[i] = -1;
            }
            this.elementCount = 0;
            this.droppedCount = 0;
            this.state = newstate;
            this.threshold = (int) (state.elementDataSize * loadFactor);
        }

    }

}



