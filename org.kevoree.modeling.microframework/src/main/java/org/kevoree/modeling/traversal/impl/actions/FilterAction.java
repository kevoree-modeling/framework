package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalAction;
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
    public void execute(KObject[] p_inputs) {
        boolean[] selectedIndex = new boolean[p_inputs.length];
        int selected = 0;
        for (int i = 0; i < p_inputs.length; i++) {
            try {
                if (_filter.filter(p_inputs[i])) {
                    selectedIndex[i] = true;
                    selected++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        KObject[] nextStepElement = new KObject[selected];
        int inserted = 0;
        for (int i = 0; i < p_inputs.length; i++) {
            if (selectedIndex[i]) {
                nextStepElement[inserted] = p_inputs[i];
                inserted++;
            }
        }
        _next.execute(nextStepElement);
    }

}

