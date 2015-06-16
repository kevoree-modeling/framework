package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class PutResult implements KMessage {

    public long id;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.PUT_RES_TYPE;
    }
}
