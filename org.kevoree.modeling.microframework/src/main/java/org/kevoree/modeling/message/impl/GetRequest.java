package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class GetRequest implements KMessage {

    public long id;

    public KContentKey[] keys;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        if (keys != null) {
            buffer.append(",");
            buffer.append("\"");
            buffer.append(KMessageLoader.KEYS_NAME).append("\":[");
            for (int i = 0; i < keys.length; i++) {
                if (i != 0) {
                    buffer.append(",");
                }
                buffer.append("\"");
                buffer.append(keys[i].toString());
                buffer.append("\"");
            }
            buffer.append("]\n");
        }
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.GET_REQ_TYPE;
    }
}
