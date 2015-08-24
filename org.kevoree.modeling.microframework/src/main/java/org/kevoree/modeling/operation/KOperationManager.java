package org.kevoree.modeling.operation;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KOperation;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.message.KMessage;

public interface KOperationManager {

    void register(KMetaOperation operation, KOperation callback, KObject target);

    void invoke(KObject source, KMetaOperation operation, Object[] param, KOperationStrategy strategy, KCallback callback);

    void dispatch(String fromPeer, KMessage message);

    void send(String peer, KMessage message, KCallback<KMessage> callback);

}
