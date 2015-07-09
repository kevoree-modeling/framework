package org.kevoree.modeling.traversal;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;

public interface KTraversalActionContext {

    KObject[] inputObjects();

    void setInputObjects(KObject[] newSet);

    KTraversalIndexResolver indexResolver();

    KCallback<Object[]> finalCallback();

}
