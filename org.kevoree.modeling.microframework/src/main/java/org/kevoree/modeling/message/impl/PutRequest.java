package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.cdn.KContentPutRequest;
import org.kevoree.modeling.cdn.impl.ContentPutRequest;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class PutRequest implements KMessage {

    public KContentPutRequest request;

    public long id;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        if (request != null) {
            buffer.append(",\"");
            buffer.append(KMessageLoader.KEYS_NAME).append("\":[");
            for (int i = 0; i < request.size(); i++) {
                if (i != 0) {
                    buffer.append(",");
                }
                buffer.append("\"");
                buffer.append(request.getKey(i));
                buffer.append("\"");
            }
            buffer.append("]\n");
            buffer.append(",\"");
            buffer.append(KMessageLoader.VALUES_NAME).append("\":[");
            for (int i = 0; i < request.size(); i++) {
                if (i != 0) {
                    buffer.append(",");
                }
                buffer.append("\"");
                buffer.append(JsonString.encode(request.getContent(i)));
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
