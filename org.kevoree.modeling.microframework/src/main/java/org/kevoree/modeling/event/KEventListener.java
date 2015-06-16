package org.kevoree.modeling.event;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMeta;

public interface KEventListener {

    void on(KObject src, KMeta[] modifications);

}
