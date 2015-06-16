package org.kevoree.modeling.operation;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.message.KMessage;

public interface KOperationManager {

    void registerOperation(KMetaOperation operation, KOperation callback, KObject target);

    void call(KObject source, KMetaOperation operation, Object[] param, KCallback<Object> callback);

    void operationEventReceived(KMessage operationEvent);
}
