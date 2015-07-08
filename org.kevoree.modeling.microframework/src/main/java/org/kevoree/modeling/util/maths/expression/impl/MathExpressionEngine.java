package org.kevoree.modeling.util.maths.expression.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.KStringMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class MathExpressionEngine {

    /**
     * All defined variables with name and value.
     */
    private KStringMap<Double> variables;

    /**
     * What character to use for decimal separators.
     */
    private final char decimalSeparator = '.';

    /**
     * What character to use for minus sign (negative values).
     */
    private final char minusSign = '-';

    public MathExpressionEngine() {
        this.variables = new ArrayStringMap<Double>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        variables.put("PI", Math.PI);
        variables.put("TRUE", 1.0);
        variables.put("FALSE", 0.0);
    }

    /**
     * Is the string a number?
     *
     * @param st The string.
     * @return <code>true</code>, if the input string is a number.
     */
    private boolean isNumber(String st) {
        if (st.charAt(0) == minusSign && st.length() == 1)
            return false;
        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (!Character.isDigit(ch) && ch != minusSign && ch != decimalSeparator) {
                return false;
            }
        }
        return true;
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
            } else if (variables.contains(token)) {
                outputQueue.add(token);
            } else if (MathEntities.getINSTANCE().functions.contains(token.toUpperCase())) {
                stack.push(token);
                lastFunction = token;
            } else if (Character.isLetter(token.charAt(0))) {
                stack.push(token);
            } else if (",".equals(token)) {
                while (!stack.isEmpty() && !"(".equals(stack.peek())) {
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
            } else if ("(".equals(token)) {
                if (previousToken != null) {
                    if (isNumber(previousToken)) {
                        throw new RuntimeException("Missing operator at character position " + tokenizer.getPos());
                    }
                }
                stack.push(token);
            } else if (")".equals(token)) {
                while (!stack.isEmpty() && !"(".equals(stack.peek())) {
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
            if ("(".equals(element) || ")".equals(element)) {
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
            } else if (variables.contains(token)) {
                stack.push(variables.get(token));
            } else if (MathEntities.getINSTANCE().functions.contains(token.toUpperCase())) {
                MathFunction f = MathEntities.getINSTANCE().functions.get(token.toUpperCase());
                double[] p = new double[f.getNumParams()];
                for (int i = f.getNumParams() - 1; i >= 0; i--) {
                    p[i] = stack.pop();
                }
                double fResult = f.eval(p);
                stack.push(fResult);
            } else {
                stack.push(new Double(token));
            }
        }
        return stack.pop();
    }


    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public MathExpressionEngine setVariable(String variable, String value) {
        variables.put(variable, new Double(value));
        return this;
    }

}