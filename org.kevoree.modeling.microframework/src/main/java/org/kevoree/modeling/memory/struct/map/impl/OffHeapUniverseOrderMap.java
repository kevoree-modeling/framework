package org.kevoree.modeling.memory.struct.map.impl;

import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.meta.KMetaModel;

/**
 * @ignore ts
 */
public class OffHeapUniverseOrderMap extends OffHeapLongLongMap implements KUniverseOrderMap {

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
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '{') {
            cursor++;
        }
        if (payload.charAt(cursor) == ',') {//className to parse
            _className = payload.substring(initPos, cursor);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != '{') {
            cursor++;
        }
        int nbElement = Integer.parseInt(payload.substring(initPos, cursor));
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
            int cleanedBegin = beginChunk;
            if (payload.charAt(beginChunk) == '"') {
                cleanedBegin++;
            }
            int cleanedMiddleChunk = middleChunk;
            if (payload.charAt(middleChunk - 1) == '"') {
                cleanedMiddleChunk--;
            }
            long loopKey = Long.parseLong(payload.substring(cleanedBegin, cleanedMiddleChunk));
            int cleanedCursor = cursor;
            if (payload.charAt(cleanedCursor - 1) == '}') {
                cleanedCursor--;
            }
            long loopVal = Long.parseLong(payload.substring(middleChunk + 1, cleanedCursor));

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
        buffer.append(elementCount);
        buffer.append('{');
        boolean isFirst = true;
        for (int i = 0; i < elementDataSize; i++) {
            if (elementData[i] != null) {
                Entry current = elementData[i];
                if (isFirst) {
                    buffer.append('"');
                    isFirst = false;
                } else {
                    buffer.append(",\"");
                }
                buffer.append(current.key);
                buffer.append("\":");
                buffer.append(current.value);
                while (current.next != null) {
                    current = current.next;
                    buffer.append(",\"");
                    buffer.append(current.key);
                    buffer.append("\":");
                    buffer.append(current.value);
                }
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    @Override
    public void free(KMetaModel metaModel) {
        this.elementData = null;
    }

}
