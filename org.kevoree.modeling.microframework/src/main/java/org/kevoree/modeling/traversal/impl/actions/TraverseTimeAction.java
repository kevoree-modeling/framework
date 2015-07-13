package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.traversal.KTraversalFilter;

public class TraverseTimeAction implements KTraversalAction {

    private KTraversalAction _next;

    private long _timeOffset;

    private long _steps;

    private KTraversalFilter _continueContition;

    public TraverseTimeAction(long p_timeOffset, long p_steps, KTraversalFilter p_continueCondition) {
        this._timeOffset = p_timeOffset;
        this._steps = p_steps;
        this._continueContition = p_continueCondition;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {
        throw new RuntimeException("Not implemented Yet!");
    }

}
