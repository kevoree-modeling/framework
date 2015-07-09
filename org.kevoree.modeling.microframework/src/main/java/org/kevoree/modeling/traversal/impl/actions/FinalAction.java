package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;

public class FinalAction implements KTraversalAction {

    private KCallback<KObject[]> _finalCallback;

    public FinalAction(KCallback<KObject[]> p_callback) {
        this._finalCallback = p_callback;
    }

    @Override
    public void chain(KTraversalAction next) {
        //terminal leaf action
    }

    @Override
    public void execute(KTraversalActionContext context) {
        context.finalCallback().on(context.inputObjects());

        _finalCallback.on(context.inputObjects());
    }
}
