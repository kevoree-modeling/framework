package org.kevoree.modeling.traversal;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KView;

public interface KTraversalActionContext {

    KObject[] inputObjects();

    void setInputObjects(KObject[] newSet);

    KCallback<Object[]> finalCallback();

    KView baseView();

}
