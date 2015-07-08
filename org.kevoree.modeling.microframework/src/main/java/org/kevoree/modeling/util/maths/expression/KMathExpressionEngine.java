package org.kevoree.modeling.util.maths.expression;

public interface KMathExpressionEngine {

    double eval(String p_expression);

    void setVarResolver(KMathVariableResolver resolver);

}
