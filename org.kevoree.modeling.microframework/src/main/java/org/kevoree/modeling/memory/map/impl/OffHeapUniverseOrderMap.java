package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;

/**
 * @ignore ts
 */
// TODO _counter and _className should be moved to offheap
public class OffHeapUniverseOrderMap extends OffHeapLongLongMap implements KUniverseOrderMap, KOffHeapMemoryElement {

    private volatile int _counter = 0;

    private String _className;

    @Override
    public final int counter() {
        return this._counter;
    }

    @Override
    public final void inc() {
        internal_counter(true);
    }

    @Override
    public final void dec() {
        internal_counter(false);
    }

    private synchronized void internal_counter(boolean inc) {
        if (inc) {
            this._counter++;
        } else {
            this._counter--;
        }
    }

    public OffHeapUniverseOrderMap(int p_initalCapacity, float p_loadFactor, String p_className) {
        super(p_initalCapacity, p_loadFactor);
        this._className = p_className;
    }

    @Override
    public String metaClassName() {
        return _className;
    }

    @Override
    public boolean isDirty() {
        return UNSAFE.getByte(_start_address + OFFSET_STARTADDRESS_DIRTY) != 0;
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        UNSAFE.putByte(_start_address + OFFSET_STARTADDRESS_DIRTY, (byte) 0);
    }

    @Override
    public void setDirty() {
        UNSAFE.putByte(_start_address + OFFSET_STARTADDRESS_DIRTY, (byte) 1);
    }

    /* warning: this method is not thread safe */
    @Override
    public void init(String payload, KMetaModel metaModel) {
        if (payload == null || payload.length() == 0) {
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '/') {
            cursor++;
        }
        if (cursor >= payload.length()) {
            return;
        }
        if (payload.charAt(cursor) == ',') {//className to parse
            _className = payload.substring(initPos, cursor);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != '/') {
            cursor++;
        }
        int nbElement = Base64.decodeToIntWithBounds(payload, initPos, cursor);
        rehashCapacity(nbElement);
        while (cursor < payload.length()) {
            cursor++;
            int beginChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ':') {
                cursor++;
            }
            int middleChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                cursor++;
            }
            long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
            long loopVal = Base64.decodeToLongWithBounds(payload, middleChunk + 1, cursor);
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
            //insert K/V
            int newIndex = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            internal_setKey(_start_address, newIndex, loopKey);
            internal_setValue(_start_address, newIndex, loopVal);
            int currentHashedIndex = internal_getHash(_start_address, index);
            if (currentHashedIndex != -1) {
                internal_setNext(_start_address, newIndex, currentHashedIndex);
            } else {
                internal_setNext(_start_address, newIndex, -2);//special char to tag used values
            }
            internal_setHash(_start_address, index, newIndex);

            // increase element count
            int oldElementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);
            int elementCount = oldElementCount + 1;
            UNSAFE.putInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT, elementCount);
        }
    }

    @Override
    public String serialize(KMetaModel metaModel) {
        int elementCount = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_COUNT);

        final StringBuilder buffer = new StringBuilder(elementCount * 8);//roughly approximate init size
        if (_className != null) {
            buffer.append(_className);
            buffer.append(',');
        }
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;

        int elementDataSize = UNSAFE.getInt(_start_address + OFFSET_STARTADDRESS_ELEM_DATA_SIZE);
        for (int i = 0; i < elementDataSize; i++) {
            if (internal_getNext(_start_address, i) != -1) { //there is a real value
                long loopKey = internal_getKey(_start_address, i);
                long loopValue = internal_getValue(_start_address, i);
                if (!isFirst) {
                    buffer.append(",");
                }
                isFirst = false;
                Base64.encodeLongToBuffer(loopKey, buffer);
                buffer.append(":");
                Base64.encodeLongToBuffer(loopValue, buffer);
            }
        }
        return buffer.toString();
    }

    @Override
    public void free(KMetaModel metaModel) {
        clear();
    }

    @Override
    public long getMemoryAddress() {
        return this._start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        this._start_address = address;
    }
}
