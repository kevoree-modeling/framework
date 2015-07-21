package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class PutRequest implements KMessage {

    public KContentKey[] keys;

    public String[] values;

    public long id;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        if (keys != null && values != null) {
            buffer.append(",\"");
            buffer.append(KMessageLoader.KEYS_NAME).append("\":[");
            for (int i = 0; i < keys.length; i++) {
                if (i != 0) {
                    buffer.append(",");
                }
                buffer.append("\"");
                buffer.append(keys[i]);
                buffer.append("\"");
            }
            buffer.append("]\n");
            buffer.append(",\"");
            buffer.append(KMessageLoader.VALUES_NAME).append("\":[");
            for (int i = 0; i < keys.length; i++) {
                if (i != 0) {
                    buffer.append(",");
                }
                buffer.append("\"");
                buffer.append(JsonString.encode(values[i]));
                buffer.append("\"");
            }
            buffer.append("]\n");
        }
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.PUT_REQ_TYPE;
    }
}
