package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;
import org.kevoree.modeling.util.maths.expression.impl.MathExpressionEngine;

public class MathExpressionAction implements KTraversalAction {

    private String _expression;

    private KMathExpressionEngine _engine;

    public MathExpressionAction(String p_expression) {
        this._expression = p_expression;
        this._engine = new MathExpressionEngine();
        this._engine.parse(p_expression);
    }

    @Override
    public void chain(KTraversalAction next) {
        //terminal leaf action
    }

    @Override
    public void execute(KTraversalActionContext context) {
        Object[] selected = new Object[context.inputObjects().length];
        for (int i = 0; i < context.inputObjects().length; i++) {
            if (context.inputObjects()[i] != null) {
                selected[i] = _engine.eval(context.inputObjects()[i]);
            }
        }
        if (context.finalCallback() != null) {
            context.finalCallback().on(selected);
        }
    }
}
