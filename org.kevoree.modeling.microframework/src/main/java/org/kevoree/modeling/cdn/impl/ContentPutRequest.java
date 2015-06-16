package org.kevoree.modeling.cdn.impl;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentPutRequest;

public class ContentPutRequest implements KContentPutRequest {

    private Object[][] _content;

    private static final int KEY_INDEX = 0;

    private static final int CONTENT_INDEX = 1;

    private static final int SIZE_INDEX = 2;

    private int _size = 0;

    public ContentPutRequest(int requestSize) {
        _content = new Object[requestSize][];
    }

    public void put(KContentKey p_key, String p_payload) {
        Object[] newLine = new Object[SIZE_INDEX];
        newLine[KEY_INDEX] = p_key;
        newLine[CONTENT_INDEX] = p_payload;
        _content[_size] = newLine;
        _size = _size + 1;
    }

    public KContentKey getKey(int index) {
        if (index < _content.length) {
            return (KContentKey) _content[index][0];
        } else {
            return null;
        }
    }

    public String getContent(int index) {
        if (index < _content.length) {
            return (String) _content[index][1];
        } else {
            return null;
        }
    }

    public int size() {
        return _size;
    }

}
