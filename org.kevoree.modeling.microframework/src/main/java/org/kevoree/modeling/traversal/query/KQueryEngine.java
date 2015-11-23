package org.kevoree.modeling.traversal.query;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.traversal.KTraversal;

public interface KQueryEngine {

    void eval(String query, KObject[] origins, KView view, KCallback<Object[]> callback);

    KTraversal buildTraversal(String query);

}
