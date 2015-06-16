package org.kevoree.modeling.format;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;

public interface KModelFormat {

    void save(KObject model, KCallback<String> cb);

    void saveRoot(KCallback<String> cb);

    void load(String payload, KCallback cb);

}