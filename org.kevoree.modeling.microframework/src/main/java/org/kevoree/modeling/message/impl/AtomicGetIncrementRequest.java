package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class AtomicGetIncrementRequest implements KMessage {

    public long id;

    public KContentKey key;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        MessageHelper.printElem(key, KMessageLoader.KEY_NAME, buffer);
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.ATOMIC_GET_INC_REQUEST_TYPE;
    }

}
