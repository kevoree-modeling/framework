package org.kevoree.modeling.operation;


import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.operation.KOperationManager;

public interface KOperationStrategy {

    void invoke(KContentDeliveryDriver cdn, KMetaOperation metaOperation, KObject source, Object[] param, KOperationManager manager, KCallback callback);

}
