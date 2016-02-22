package org.kevoree.modeling.defer;

import org.kevoree.modeling.KCallback;

public interface KCounterDefer {

    void countDown();

    void then(KCallback callback);

}
