package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.impl.MetaLiteral;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.util.PrimitiveHelper;
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
                        if (PrimitiveHelper.equals(potentialVarName, "PI")) {
                            return Math.PI;
                        }
                        if (PrimitiveHelper.equals(potentialVarName, "TRUE")) {
                            return 1.0;
                        }
                        if (PrimitiveHelper.equals(potentialVarName, "FALSE")) {
                            return 0.0;
                        }
                        KObject loopObj = context.inputObjects()[finalI];
                        if (PrimitiveHelper.equals(potentialVarName, "TIME_MS_NS")) {
                            return (double) (loopObj.now() / 1000000);
                        }
                        if (PrimitiveHelper.equals(potentialVarName, "TIME_NS_MS")) {
                            return (double) (loopObj.now() * 1000000);
                        }
                        if (PrimitiveHelper.equals(potentialVarName, "TIME")) {
                            return (double) loopObj.now();
                        }
                        Object resolved = loopObj.getByName(potentialVarName);
                        if (resolved != null) {
                            if (resolved instanceof MetaLiteral) {
                                return (double) ((MetaLiteral) resolved).index();
                            } else {
                                String valueString = resolved.toString();
                                if (PrimitiveHelper.equals(valueString, "true")) {
                                    return 1.0;
                                } else if (PrimitiveHelper.equals(valueString, "false")) {
                                    return 0.0;
                                } else {
                                    try {
                                        return PrimitiveHelper.parseDouble(resolved.toString());
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
