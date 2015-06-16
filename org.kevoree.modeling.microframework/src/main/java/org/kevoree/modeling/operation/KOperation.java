package org.kevoree.modeling.operation;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;

public interface KOperation {

    void on(KObject source, Object[] params, KCallback<Object> result);

}
