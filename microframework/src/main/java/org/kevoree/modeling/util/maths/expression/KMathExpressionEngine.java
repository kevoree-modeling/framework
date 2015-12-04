package org.kevoree.modeling.util.maths.expression;

import org.kevoree.modeling.KObject;

public interface KMathExpressionEngine {

    KMathExpressionEngine parse(String p_expression);

    void setVarResolver(KMathVariableResolver resolver);

    double eval(KObject context);

}
