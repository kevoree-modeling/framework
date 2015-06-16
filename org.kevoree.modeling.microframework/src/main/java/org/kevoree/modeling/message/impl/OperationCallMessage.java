package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class OperationCallMessage implements KMessage {

    public long id;
    public int classIndex;
    public int opIndex;
    public String[] params;
    public KContentKey key;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        MessageHelper.printElem(key, KMessageLoader.KEY_NAME, buffer);
        buffer.append(",\"").append(KMessageLoader.CLASS_IDX_NAME).append("\":\"").append(classIndex).append("\"");
        buffer.append(",\"").append(KMessageLoader.OPERATION_NAME).append("\":\"").append(opIndex).append("\"");
        if (params != null) {
            buffer.append(",\"");
            buffer.append(KMessageLoader.PARAMETERS_NAME).append("\":[");
            for (int i = 0; i < params.length; i++) {
                if (i != 0) {
                    buffer.append(",");
                }
                buffer.append("\"");
                buffer.append(JsonString.encode(params[i]));
                buffer.append("\"");
            }
            buffer.append("]\n");
        }
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.OPERATION_CALL_TYPE;
    }
}
