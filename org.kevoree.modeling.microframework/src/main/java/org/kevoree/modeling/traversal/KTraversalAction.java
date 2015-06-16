package org.kevoree.modeling.traversal;

import org.kevoree.modeling.KObject;

public interface KTraversalAction {

    void chain(KTraversalAction next);

    void execute(KObject[] inputs);

}
