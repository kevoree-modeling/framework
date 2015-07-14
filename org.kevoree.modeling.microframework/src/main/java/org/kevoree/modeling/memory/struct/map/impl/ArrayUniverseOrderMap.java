package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
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
 */
public class ArrayUniverseOrderMap extends ArrayLongLongMap implements KUniverseOrderMap {

    private int _counter = 0;

    private String _className;

    @Override
    public int counter() {
        return _counter;
    }

    @Override
    public void inc() {
        _counter++;
    }

    @Override
    public void dec() {
        _counter--;
    }

    public ArrayUniverseOrderMap(int p_initalCapacity, float p_loadFactor, String p_className) {
        super(p_initalCapacity, p_loadFactor);
        this._className = p_className;
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
            //insert K/V
            Entry entry = null;
            int index = -1;
            int hash = (int) (loopKey);
            if (elementDataSize != 0) {
                index = (hash & 0x7FFFFFFF) % elementDataSize;
                entry = findNonNullKeyEntry(loopKey, index);
            }
            if (entry == null) {
                if (++elementCount > threshold) {
                    rehash();
                    index = (hash & 0x7FFFFFFF) % elementDataSize;
                }
                entry = createHashedEntry(loopKey, index);
            }
            entry.value = loopVal;

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
        for (int i = 0; i < elementDataSize; i++) {
            if (elementData[i] != null) {
                Entry current = elementData[i];
                if (!isFirst) {
                    buffer.append(",");
                }
                isFirst = false;
                Base64.encodeLongToBuffer(current.key, buffer);
                buffer.append(":");
                Base64.encodeLongToBuffer(current.value, buffer);
                while (current.next != null) {
                    current = current.next;
                    buffer.append(",");
                    Base64.encodeLongToBuffer(current.key, buffer);
                    buffer.append(":");
                    Base64.encodeLongToBuffer(current.value, buffer);
                }
            }
        }
        return buffer.toString();
    }

    @Override
    public void free(KMetaModel metaModel) {
        this.elementData = null;
    }

}
