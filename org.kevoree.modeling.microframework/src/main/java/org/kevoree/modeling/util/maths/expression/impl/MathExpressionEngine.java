package org.kevoree.modeling.util.maths.expression.impl;

import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.expression.KMathExpressionEngine;
import org.kevoree.modeling.util.maths.expression.KMathVariableResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class MathExpressionEngine implements KMathExpressionEngine {

    private KMathVariableResolver varResolver;

    public static final char decimalSeparator = '.';
    public static final char minusSign = '-';

    public MathExpressionEngine() {
        varResolver = new KMathVariableResolver() {
            @Override
            public Double resolve(String potentialVarName) {
                if (PrimitiveHelper.equals(potentialVarName,"PI")) {
                    return Math.PI;
                }
                if (PrimitiveHelper.equals(potentialVarName,"TRUE")) {
                    return 1.0;
                }
                if (PrimitiveHelper.equals(potentialVarName,"FALSE")) {
                    return 0.0;
                }
                return null;
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
            } else if (PrimitiveHelper.equals(",",token)) {
                while (!stack.isEmpty() && !PrimitiveHelper.equals("(",stack.peek())) {
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
            } else if (PrimitiveHelper.equals("(",token)) {
                if (previousToken != null) {
                    if (isNumber(previousToken)) {
                        throw new RuntimeException("Missing operator at character position " + tokenizer.getPos());
                    }
                }
                stack.push(token);
            } else if (PrimitiveHelper.equals(")", token)) {
                while (!stack.isEmpty() && !PrimitiveHelper.equals("(",stack.peek())) {
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
            if (PrimitiveHelper.equals("(",element) || PrimitiveHelper.equals(")",element)) {
                throw new RuntimeException("Mismatched parentheses");
            }
            if (!MathEntities.getINSTANCE().operators.contains(element)) {
                throw new RuntimeException("Unknown operator or function: "
                        + element);
            }
            outputQueue.add(element);
        }
        return outputQueue;
    }

    /**
     * Evaluates the expression.
     *
     * @return The result of the expression.
     */
    public double eval(String p_expression) {
        List<String> rpn = shuntingYard(p_expression);
        Stack<Double> stack = new Stack<Double>();
        for (int ii = 0; ii < rpn.size(); ii++) {
            String token = rpn.get(ii);
            if (MathEntities.getINSTANCE().operators.contains(token)) {
                double v1 = stack.pop();
                double v2 = stack.pop();
                stack.push(MathEntities.getINSTANCE().operators.get(token).eval(v2, v1));
            } else if (varResolver.resolve(token) != null ) {
                stack.push(varResolver.resolve(token));
            } else if (MathEntities.getINSTANCE().functions.contains(token.toUpperCase())) {
                MathFunction f = MathEntities.getINSTANCE().functions.get(token.toUpperCase());
                double[] p = new double[f.getNumParams()];
                for (int i = f.getNumParams() - 1; i >= 0; i--) {
                    p[i] = stack.pop();
                }
                double fResult = f.eval(p);
                stack.push(fResult);
            } else {
                stack.push(PrimitiveHelper.parseDouble(token));
            }
        }
        return stack.pop();
    }

    @Override
    public void setVarResolver(KMathVariableResolver p_resolver) {
        this.varResolver = p_resolver;
    }

}