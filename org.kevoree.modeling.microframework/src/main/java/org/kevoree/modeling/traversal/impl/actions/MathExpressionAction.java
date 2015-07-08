package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;
import org.kevoree.modeling.util.maths.expression.KMathVariableResolver;
import org.kevoree.modeling.util.maths.expression.impl.MathExpressionEngine;

public class MathExpressionAction implements KTraversalAction {

    private KCallback<Object[]> _finalCallback;

    private String _expression;

    private KMathExpressionEngine _engine;

    public MathExpressionAction(String p_expression, KCallback<Object[]> p_callback) {
        this._finalCallback = p_callback;
        this._expression = p_expression;
        this._engine = new MathExpressionEngine();
    }

    @Override
    public void chain(KTraversalAction next) {
        //terminal leaf action
    }

    @Override
    public void execute(KObject[] inputs) {
        Object[] selected = new Object[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] != null) {
                final int finalI = i;
                _engine.setVarResolver(new KMathVariableResolver() {
                    @Override
                    public Double resolve(String potentialVarName) {
                        if (potentialVarName.equals("PI")) {
                            return Math.PI;
                        }
                        if (potentialVarName.equals("TRUE")) {
                            return 1.0;
                        }
                        if (potentialVarName.equals("FALSE")) {
                            return 0.0;
                        }
                        Object resolved = inputs[finalI].getByName(potentialVarName);
                        if (resolved != null) {
                            try {
                                return Double.parseDouble(resolved.toString());
                            } catch (Exception e) {
                                //noop
                            }
                        }
                        return null;
                    }
                });
                selected[finalI] = _engine.eval(this._expression);
            }
        }
        _finalCallback.on(selected);
    }
}
