package org.kevoree.modeling.traversal;

public interface KTraversalAction {

    void chain(KTraversalAction next);

    void execute(KTraversalActionContext context);

}
