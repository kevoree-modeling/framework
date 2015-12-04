package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.traversal.KTraversalFilter;

public class FilterAction implements KTraversalAction {

    private KTraversalAction _next;

    private KTraversalFilter _filter;

    public FilterAction(KTraversalFilter p_filter) {
        this._filter = p_filter;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {
        boolean[] selectedIndex = new boolean[context.inputObjects().length];
        int selected = 0;
        for (int i = 0; i < context.inputObjects().length; i++) {
            try {
                if (_filter.filter(context.inputObjects()[i])) {
                    selectedIndex[i] = true;
                    selected++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        KObject[] nextStepElement = new KObject[selected];
        int inserted = 0;
        for (int i = 0; i < context.inputObjects().length; i++) {
            if (selectedIndex[i]) {
                nextStepElement[inserted] = context.inputObjects()[i];
                inserted++;
            }
        }
        if (_next == null) {
            context.finalCallback().on(nextStepElement);
        } else {
            context.setInputObjects(nextStepElement);
            _next.execute(context);
        }
    }

}

