package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;
import org.kevoree.modeling.util.maths.Base64;

public class Events implements KMessage {

    public long[] _keys;

    public long[] allKeys() {
        return _keys;
    }

    public Events(long[] p_keys) {
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
            Base64.encodeLongToBuffer(_keys[i], buffer);
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
