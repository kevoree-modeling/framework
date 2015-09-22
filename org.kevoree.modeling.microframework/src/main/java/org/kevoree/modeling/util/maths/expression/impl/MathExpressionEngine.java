package org.kevoree.modeling.util.maths.expression.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.impl.MetaLiteral;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;
import org.kevoree.modeling.util.maths.expression.KMathVariableResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;


public class MathExpressionEngine implements KMathExpressionEngine {

    private KMathVariableResolver varResolver;

    public static final char decimalSeparator = '.';
    public static final char minusSign = '-';

    public MathExpressionEngine() {

        HashMap<String, Double> vars = new HashMap<String, Double>();
        vars.put("PI", Math.PI);
        vars.put("TRUE", 1.0);
        vars.put("FALSE", 0.0);

        varResolver = new KMathVariableResolver() {
            @Override
            public Double resolve(String potentialVarName) {
                return vars.get(potentialVarName);
            }
        };
    }

    /**
     * @native ts
     * return !isNaN(+st);
     */
    public static boolean isNumber(String st) {
        if (st.charAt(0) == minusSign && st.length() == 1)
            return false;
        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (!isDigit(ch) && ch != minusSign && ch != decimalSeparator) {
                return false;
            }
        }
        return true;
    }

    /**
     * @native ts
     * var cc = c.charCodeAt(0);
     * if ( cc >= 0x30 && cc <= 0x39 ){
     * return true ;
     * }
     * return false ;
     */
    public static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    /**
     * @native ts
     * var cc = c.charCodeAt(0);
     * if ( ( cc >= 0x41 && cc <= 0x5A ) || ( cc >= 0x61 && cc <= 0x7A ) ){
     * return true ;
     * }
     * return false ;
     */
    public static boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    /**
     * @native ts
     * var cc = c.charCodeAt(0);
     * if ( ( cc >= 0x0009 && cc <= 0x000D ) || ( cc == 0x0020 ) || ( cc == 0x0085 ) || ( cc == 0x00A0 ) ){
     * return true ;
     * }
     * return false ;
     */
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }


    /**
     * Implementation of the <i>Shunting Yard</i> algorithm to transform an
     * infix expression to a RPN expression.
     *
     * @param expression The input expression in infx.
     * @return A RPN representation of the expression, with each token as a list
     * member.
     */
    private List<String> shuntingYard(String expression) {
        List<String> outputQueue = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();
        MathExpressionTokenizer tokenizer = new MathExpressionTokenizer(expression);
        String lastFunction = null;
        String previousToken = null;
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (isNumber(token)) {
                outputQueue.add(token);
            } else if (varResolver.resolve(token) != null) {
                outputQueue.add(token);
            } else if (MathEntities.getINSTANCE().functions.contains(token.toUpperCase())) {
                stack.push(token);
                lastFunction = token;
            } else if (isLetter(token.charAt(0))) {
                stack.push(token);
            } else if (PrimitiveHelper.equals(",", token)) {
                while (!stack.isEmpty() && !PrimitiveHelper.equals("(", stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new RuntimeException("Parse error for function '"
                            + lastFunction + "'");
                }
            } else if (MathEntities.getINSTANCE().operators.contains(token)) {
                MathOperation o1 = MathEntities.getINSTANCE().operators.get(token);
                String token2 = stack.isEmpty() ? null : stack.peek();
                while (MathEntities.getINSTANCE().operators.contains(token2)
                        && ((o1.isLeftAssoc() && o1.getPrecedence() <= MathEntities.getINSTANCE().operators
                        .get(token2).getPrecedence()) || (o1
                        .getPrecedence() < MathEntities.getINSTANCE().operators.get(token2)
                        .getPrecedence()))) {
                    outputQueue.add(stack.pop());
                    token2 = stack.isEmpty() ? null : stack.peek();
                }
                stack.push(token);
            } else if (PrimitiveHelper.equals("(", token)) {
                if (previousToken != null) {
                    if (isNumber(previousToken)) {
                        throw new RuntimeException("Missing operator at character position " + tokenizer.getPos());
                    }
                }
                stack.push(token);
            } else if (PrimitiveHelper.equals(")", token)) {
                while (!stack.isEmpty() && !PrimitiveHelper.equals("(", stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new RuntimeException("Mismatched parentheses");
                }
                stack.pop();
                if (!stack.isEmpty()
                        && MathEntities.getINSTANCE().functions.contains(stack.peek().toUpperCase())) {
                    outputQueue.add(stack.pop());
                }
            }
            previousToken = token;
        }
        while (!stack.isEmpty()) {
            String element = stack.pop();
            if (PrimitiveHelper.equals("(", element) || PrimitiveHelper.equals(")", element)) {
                throw new RuntimeException("Mismatched parentheses");
            }
            outputQueue.add(element);
        }
        return outputQueue;
    }


    @Override
    public double eval(KObject context) {
        if (this._cacheAST == null) {
            throw new RuntimeException("Call parse before");
        }
        Stack<Double> stack = new Stack<Double>();
        for (int ii = 0; ii < _cacheAST.length; ii++) {
            MathToken mathToken = _cacheAST[ii];
            switch (mathToken.type()) {
                case 0:
                    double v1 = stack.pop();
                    double v2 = stack.pop();
                    MathOperation castedOp = (MathOperation) mathToken;
                    stack.push(castedOp.eval(v2, v1));
                    break;
                case 1:
                    MathFunction castedFunction = (MathFunction) mathToken;
                    double[] p = new double[castedFunction.getNumParams()];
                    for (int i = castedFunction.getNumParams() - 1; i >= 0; i--) {
                        p[i] = stack.pop();
                    }
                    stack.push(castedFunction.eval(p));
                    break;
                case 2:
                    MathDoubleToken castedDouble = (MathDoubleToken) mathToken;
                    stack.push(castedDouble.content());
                    break;
                case 3:
                    MathFreeToken castedFreeToken = (MathFreeToken) mathToken;
                    if (varResolver.resolve(castedFreeToken.content()) != null) {
                        stack.push(varResolver.resolve(castedFreeToken.content()));
                    } else {
                        if (context != null) {
                            if ("TIME".equals(castedFreeToken.content())) {
                                stack.push((double) context.now());
                            } else {
                                Object resolved = context.getByName(castedFreeToken.content());
                                if (resolved != null) {
                                    if (resolved instanceof MetaLiteral) {
                                        stack.push((double) ((MetaLiteral) resolved).index());
                                    } else {
                                        String valueString = resolved.toString();
                                        if (PrimitiveHelper.equals(valueString, "true")) {
                                            stack.push(1.0);
                                        } else if (PrimitiveHelper.equals(valueString, "false")) {
                                            stack.push(0.0);
                                        } else {
                                            try {
                                                stack.push(PrimitiveHelper.parseDouble(resolved.toString()));
                                            } catch (Exception e) {
                                                //noop
                                            }
                                        }
                                    }
                                } else {
                                    throw new RuntimeException("Unknow variable for name " + castedFreeToken.content());
                                }
                            }
                        } else {
                            throw new RuntimeException("Unknow variable for name " + castedFreeToken.content());
                        }
                    }
                    break;
            }
        }
        Double result = stack.pop();
        if (result == null) {
            return 0;
        } else {
            return result;
        }
    }

    private MathToken[] _cacheAST = null;

    public MathToken[] buildAST(List<String> rpn) {
        MathToken[] result = new MathToken[rpn.size()];
        for (int ii = 0; ii < rpn.size(); ii++) {
            String token = rpn.get(ii);
            if (MathEntities.getINSTANCE().operators.contains(token)) {
                result[ii] = MathEntities.getINSTANCE().operators.get(token);
            } else if (MathEntities.getINSTANCE().functions.contains(token.toUpperCase())) {
                result[ii] = MathEntities.getINSTANCE().functions.get(token.toUpperCase());
            } else {
                try {
                    double parsed = PrimitiveHelper.parseDouble(token);
                    result[ii] = new MathDoubleToken(parsed);
                } catch (Exception e) {
                    result[ii] = new MathFreeToken(token);
                }
            }
        }
        return result;
    }

    @Override
    public KMathExpressionEngine parse(String p_expression) {
        List<String> rpn = shuntingYard(p_expression);
        _cacheAST = buildAST(rpn);
        return this;
    }

    @Override
    public void setVarResolver(KMathVariableResolver p_resolver) {
        this.varResolver = p_resolver;
    }

}