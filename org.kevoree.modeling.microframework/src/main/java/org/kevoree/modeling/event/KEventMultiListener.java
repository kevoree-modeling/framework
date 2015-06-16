package org.kevoree.modeling.event;

import org.kevoree.modeling.KObject;

public interface KEventMultiListener {

    void on(KObject[] objects);

}
