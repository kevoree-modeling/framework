package org.kevoree.modeling.memory.map.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.map.KUniverseOrderMap;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;

/**
 * @native ts
 * private _counter = 0;
 * private _className : string;
 * constructor(initalCapacity: number, loadFactor : number, p_className : string) { super(initalCapacity,loadFactor);this._className = p_className; }
 * public metaClassName(){ return this._className; }
 * public counter():number { return this._counter; }
 * public inc():void { this._counter++; }
 * public dec():void { this._counter--; }
 * public free():void {  }
 * public size():number { return Object.keys(this).length-3; }
 * public serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string {
 * var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
 * if (this._className != null) {buffer.append(this._className);buffer.append(',');}
 * org.kevoree.modeling.util.maths.Base64.encodeIntToBuffer(this.size(),buffer);
 * buffer.append('/');
 * var isFirst: boolean = true;
 * for (var propKey in this) { if(this.hasOwnProperty(propKey) && propKey[0] != '_'){
 * if (!isFirst) {buffer.append(",");} isFirst = false;
 * org.kevoree.modeling.util.maths.Base64.encodeLongToBuffer(propKey, buffer);
 * buffer.append(":");
 * org.kevoree.modeling.util.maths.Base64.encodeLongToBuffer(this[propKey], buffer);
 * }}
 * return buffer.toString();
 * }
 * public init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void {
 * if (payload == null || payload.length == 0) { return; }
 * var initPos: number = 0; var cursor: number = 0;
 * while (cursor < payload.length && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '/'){ cursor++; }
 * if (cursor >= payload.length) { return; }
 * if (payload.charAt(cursor) == ',') { this._className = payload.substring(initPos, cursor);cursor++;initPos = cursor;}
 * while (cursor < payload.length && payload.charAt(cursor) != '/'){cursor++;}
 * var nbElement: number = java.lang.Integer.parseInt(payload.substring(initPos, cursor));
 * while (cursor < payload.length){
 * cursor++;
 * var beginChunk: number = cursor;
 * while (cursor < payload.length && payload.charAt(cursor) != ':'){cursor++;}
 * var middleChunk: number = cursor;
 * while (cursor < payload.length && payload.charAt(cursor) != ','){cursor++;}
 * var loopKey: number = org.kevoree.modeling.util.maths.Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
 * var loopVal: number = org.kevoree.modeling.util.maths.Base64.decodeToLongWithBounds(payload, middleChunk + 1, cursor);
 * this[loopKey] = loopVal;
 * }
 * }
 * public type(): number { return org.kevoree.modeling.memory.storage.MemoryElementTypes.LONG_LONG_MAP; }
 */
public class ArrayUniverseOrderMap extends ArrayLongLongMap implements KUniverseOrderMap {

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

    public ArrayUniverseOrderMap() {
        super(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    @Override
    public String metaClassName() {
        return _className;
    }

    @Override
    public boolean isDirty() {
        return _isDirty;
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        _isDirty = false;
    }

    @Override
    public void setDirty() {
        this._isDirty = true;
    }

    /* warning: this method is not thread safe */
    @Override
    public void init(String payload, KMetaModel metaModel, int metaClassIndex) {

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
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % state.elementDataSize;
            //insert K/V
            int newIndex = this.elementCount;
            state.elementKV[newIndex * 2] = loopKey;
            state.elementKV[newIndex * 2 + 1] = loopVal;
            int currentHashedIndex = state.elementHash[index];
            if (currentHashedIndex != -1) {
                state.elementNext[newIndex] = currentHashedIndex;
            } else {
                state.elementNext[newIndex] = -2; //special char to tag used values
            }
            state.elementHash[index] = newIndex;
            this.elementCount++;
        }
    }

    @Override
    public String serialize(KMetaModel metaModel) {
        final StringBuilder buffer = new StringBuilder(elementCount * 8);//roughly approximate init size
        if (_className != null) {
            buffer.append(_className);
            buffer.append(',');
        }
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        InternalState internalState = state;
        for (int i = 0; i < internalState.elementNext.length; i++) {
            if (internalState.elementNext[i] != -1) { //there is a real value
                long loopKey = internalState.elementKV[i * 2];
                long loopValue = internalState.elementKV[i * 2 + 1];
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
    public short type() {
        return KMemoryElementTypes.LONG_LONG_MAP;
    }

}
