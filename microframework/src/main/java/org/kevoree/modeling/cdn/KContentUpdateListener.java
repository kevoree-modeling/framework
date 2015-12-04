package org.kevoree.modeling.cdn;

import org.kevoree.modeling.message.KMessage;

public interface KContentUpdateListener {

    void onKeysUpdate(long[] updatedKeys);

    void onOperationCall(KMessage operationCallMessage);

}
