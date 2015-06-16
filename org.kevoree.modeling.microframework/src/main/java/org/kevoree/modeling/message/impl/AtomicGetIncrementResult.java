package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;

public class AtomicGetIncrementResult implements KMessage {

    public long id;

    public Short value;

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        MessageHelper.printJsonStart(buffer);
        MessageHelper.printType(buffer, type());
        MessageHelper.printElem(id, KMessageLoader.ID_NAME, buffer);
        MessageHelper.printElem(value, KMessageLoader.VALUE_NAME, buffer);
        MessageHelper.printJsonEnd(buffer);
        return buffer.toString();
    }

    @Override
    public int type() {
        return KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE;
    }
}
