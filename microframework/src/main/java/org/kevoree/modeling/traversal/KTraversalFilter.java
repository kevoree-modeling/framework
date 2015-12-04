package org.kevoree.modeling.traversal;

import org.kevoree.modeling.KObject;

public interface KTraversalFilter {

    boolean filter(KObject obj);

}
