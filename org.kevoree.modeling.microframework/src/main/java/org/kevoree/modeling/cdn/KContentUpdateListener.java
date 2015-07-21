package org.kevoree.modeling.cdn;

import org.kevoree.modeling.KContentKey;

public interface KContentUpdateListener {

    void on(KContentKey[] updatedKeys);

}
