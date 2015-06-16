package org.kevoree.modeling.cdn;

import org.kevoree.modeling.KContentKey;

public interface KContentPutRequest {

    void put(KContentKey p_key, String p_payload);

    KContentKey getKey(int index);

    String getContent(int index);

    int size();

}
