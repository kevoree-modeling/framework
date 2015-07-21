package org.kevoree.modeling.defer;

import org.kevoree.modeling.KCallback;

public interface KDefer {

    KCallback waitResult();

    void then(KCallback<Object[]> cb);

}
