package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.meta.impl.MetaLiteral;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;
import org.kevoree.modeling.util.maths.expression.KMathVariableResolver;
import org.kevoree.modeling.util.maths.expression.impl.MathExpressionEngine;

public class MathExpressionAction implements KTraversalAction {

    private String _expression;

    private KMathExpressionEngine _engine;

    public MathExpressionAction(String p_expression) {
        this._expression = p_expression;
        this._engine = new MathExpressionEngine();
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
                        Object resolved = context.inputObjects()[finalI].getByName(potentialVarName);
                        if (resolved != null) {
                            if (resolved instanceof MetaLiteral) {
                                return (double) ((MetaLiteral) resolved).index();
                            } else {
                                String valueString = resolved.toString();
                                if(valueString.equals("true")){
                                    return 1.0;
                                } else if(valueString.equals("false")){
                                    return 0.0;
                                } else {
                                    try {
                                        return Double.parseDouble(resolved.toString());
                                    } catch (Exception e) {
                                        //noop
                                    }
                                }
                            }
                        }
                        return null;
                    }
                });
                selected[finalI] = _engine.eval(this._expression);
            }
        }
        if (context.finalCallback() != null) {
            context.finalCallback().on(selected);
        }
    }
}
