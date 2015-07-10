package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;

public class TraverseTimeAction implements KTraversalAction {

    private KTraversalAction _next;

    public TraverseTimeAction() {
        
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {

    }

}
