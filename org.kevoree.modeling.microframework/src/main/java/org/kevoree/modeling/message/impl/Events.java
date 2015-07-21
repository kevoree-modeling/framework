package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class Events implements KMessage {

    public KContentKey[] _keys;

    public KContentKey[] allKeys() {
        return _keys;
    }

    public Events(KContentKey[] p_keys) {
        this._keys = p_keys;
    }

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        buffer.append(",");
        buffer.append("\"");
        buffer.append(KMessageLoader.KEYS_NAME).append("\":[");
        for (int i = 0; i < _keys.length; i++) {
            if (i != 0) {
                buffer.append(",");
            }
            buffer.append("\"");
            buffer.append(_keys[i]);
            buffer.append("\"");
        }
        buffer.append("]\n");
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.EVENTS_TYPE;
    }

}
