package org.kevoree.modeling.util.maths;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Iterator;
        import java.util.List;
        import java.util.Map;
        import java.util.Stack;

public class Expression {


    /**
     * The original infix expression.
     */
    private String expression = null;

    /**
     * The cached RPN (Reverse Polish Notation) of the expression.
     */
    private List<String> rpn = null;

    /**
     * All defined operators with name and implementation.
     */
    private Map<String, Operator> operators = new HashMap<String, Expression.Operator>();

    /**
     * All defined functions with name and implementation.
     */
    private Map<String, Function> functions = new HashMap<String, Expression.Function>();

    /**
     * All defined variables with name and value.
     */
    private Map<String, Double> variables = new HashMap<String, Double>();

    /**
     * What character to use for decimal separators.
     */
    private final char decimalSeparator = '.';

    /**
     * What character to use for minus sign (negative values).
     */
    private final char minusSign = '-';

    /**
     * The expression evaluators exception class.
     */
    public class ExpressionException extends RuntimeException {

        public ExpressionException(String message) {
            super(message);
        }
    }

    /**
     * Abstract definition of a supported expression function. A function is
     * defined by a name, the number of parameters and the actual processing
     * implementation.
     */
    public abstract class Function {
        /**
         * Name of this function.
         */
        private String name;
        /**
         * Number of parameters expected for this function.
         */
        private int numParams;

        /**
         * Creates a new function with given name and parameter count.
         *
         * @param name
         *            The name of the function.
         * @param numParams
         *            The number of parameters for this function.
         */
        public Function(String name, int numParams) {
            this.name = name.toUpperCase();
            this.numParams = numParams;
        }

        public String getName() {
            return name;
        }

        public int getNumParams() {
            return numParams;
        }

        public abstract double eval(List<Double> parameters);
    }

    /**
     * Abstract definition of a supported operator. An operator is defined by
     * its name (pattern), precedence and if it is left- or right associative.
     */
    public abstract class Operator {
        /**
         * This operators name (pattern).
         */
        private String oper;
        /**
         * Operators precedence.
         */
        private int precedence;
        /**
         * Operator is left associative.
         */
        private boolean leftAssoc;

        /**
         * Creates a new operator.
         *
         * @param oper
         *            The operator name (pattern).
         * @param precedence
         *            The operators precedence.
         * @param leftAssoc
         *            <code>true</code> if the operator is left associative,
         *            else <code>false</code>.
         */
        public Operator(String oper, int precedence, boolean leftAssoc) {
            this.oper = oper;
            this.precedence = precedence;
            this.leftAssoc = leftAssoc;
        }

        public String getOper() {
            return oper;
        }

        public int getPrecedence() {
            return precedence;
        }

        public boolean isLeftAssoc() {
            return leftAssoc;
        }

        /**
         * Implementation for this operator.
         *
         * @param v1
         *            Operand 1.
         * @param v2
         *            Operand 2.
         * @return The result of the operation.
         */
        public abstract double eval(double v1, double v2);
    }

    /**
     * Expression tokenizer that allows to iterate over a {@link String}
     * expression token by token. Blank characters will be skipped.
     */
    private class Tokenizer implements Iterator<String> {

        /**
         * Actual position in expression string.
         */
        private int pos = 0;

        /**
         * The original input expression.
         */
        private String input;
        /**
         * The previous token or <code>null</code> if none.
         */
        private String previousToken;

        /**
         * Creates a new tokenizer for an expression.
         *
         * @param input
         *            The expression string.
         */
        public Tokenizer(String input) {
            this.input = input.trim();
        }

        @Override
        public boolean hasNext() {
            return (pos < input.length());
        }

        /**
         * Peek at the next character, without advancing the iterator.
         *
         * @return The next character or character 0, if at end of string.
         */
        private char peekNextChar() {
            if (pos < (input.length() - 1)) {
                return input.charAt(pos + 1);
            } else {
                return 0;
            }
        }

        @Override
        public String next() {
            StringBuilder token = new StringBuilder();
            if (pos >= input.length()) {
                return previousToken = null;
            }
            char ch = input.charAt(pos);
            while (Character.isWhitespace(ch) && pos < input.length()) {
                ch = input.charAt(++pos);
            }
            if (Character.isDigit(ch)) {
                while ((Character.isDigit(ch) || ch == decimalSeparator)
                        && (pos < input.length())) {
                    token.append(input.charAt(pos++));
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                }
            } else if (ch == minusSign
                    && Character.isDigit(peekNextChar())
                    && ("(".equals(previousToken) || ",".equals(previousToken)
                    || previousToken == null || operators
                    .containsKey(previousToken))) {
                token.append(minusSign);
                pos++;
                token.append(next());
            } else if (Character.isLetter(ch) || (ch == '_')) {
                while ((Character.isLetter(ch) || Character.isDigit(ch) || (ch == '_')) && (pos < input.length())) {
                    token.append(input.charAt(pos++));
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                }
            } else if (ch == '(' || ch == ')' || ch == ',') {
                token.append(ch);
                pos++;
            } else {
                while (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_'
                        && !Character.isWhitespace(ch) && ch != '('
                        && ch != ')' && ch != ',' && (pos < input.length())) {
                    token.append(input.charAt(pos));
                    pos++;
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                    if (ch == minusSign) {
                        break;
                    }
                }
                if (!operators.containsKey(token.toString())) {
                    throw new ExpressionException("Unknown operator '" + token
                            + "' at position " + (pos - token.length() + 1));
                }
            }
            return previousToken = token.toString();
        }

        @Override
        public void remove() {
            throw new ExpressionException("remove() not supported");
        }

        /**
         * Get the actual character position in the string.
         *
         * @return The actual character position.
         */
        public int getPos() {
            return pos;
        }

    }

    /**
     * Creates a new expression instance from an expression string.
     *
     * @param expression
     *            The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
     *            <code>"sin(y)>0 & max(z, 3)>3"</code>
     */
    public Expression(String expression) {
        this.expression = expression;
        addOperator(new Operator("+", 20, true) {
            @Override
            public double eval(double v1, double v2) {
                return v1+v2;
            }
        });
        addOperator(new Operator("-", 20, true) {
            @Override
            public double eval(double v1, double v2) {
                return v1-v2;
            }
        });
        addOperator(new Operator("*", 30, true) {
            @Override
            public double eval(double v1, double v2) {
                return v1*v2;
            }
        });
        addOperator(new Operator("/", 30, true) {
            @Override
            public double eval(double v1, double v2) {
                return v1/v2;
            }
        });
        addOperator(new Operator("%", 30, true) {
            @Override
            public double eval(double v1, double v2) {
                return v1%v2;
            }
        });
        addOperator(new Operator("^", 40, false) {
            @Override
            public double eval(double v1, double v2) {				
                return Math.pow(v1,v2);
            }
        });
        addOperator(new Operator("&&", 4, false) {
            @Override
            public double eval(double v1, double v2) {
                boolean b1 = !(v1==0);
                boolean b2 = !(v2==0);
                return b1 && b2 ? 1 : 0;
            }
        });

        addOperator(new Operator("||", 2, false) {
            @Override
            public double eval(double v1, double v2) {
                boolean b1 = !(v1==0);
                boolean b2 = !(v2==0);
                return b1 || b2 ? 1 : 0;
            }
        });

        addOperator(new Operator(">", 10, false) {
            @Override
            public double eval(double v1, double v2) {
                return v1>v2  ? 1 : 0;
            }
        });

        addOperator(new Operator(">=", 10, false) {
            @Override
            public double eval(double v1, double v2) {
                return v1>=v2 ? 1 : 0;
            }
        });

        addOperator(new Operator("<", 10, false) {
            @Override
            public double eval(double v1, double v2) {
                return v1 < v2 ? 1 : 0;
            }
        });

        addOperator(new Operator("<=", 10, false) {
            @Override
            public double eval(double v1, double v2) {
                return v1 <= v2 ? 1 : 0;
            }
        });

        addOperator(new Operator("==", 7, false) {
            @Override
            public double eval(double v1, double v2) {
                return v1==v2 ? 1 : 0;
            }
        });

        addOperator(new Operator("!=", 7, false) {
            @Override
            public double eval(double v1, double v2) {
                return v1 != v2 ? 1 : 0;
            }
        });

        addFunction(new Function("NOT", 1) {
            @Override
            public double eval(List<Double> parameters) {
                boolean zero = parameters.get(0) == 0;
                return zero ? 1 : 0;
            }
        });

        addFunction(new Function("IF", 3) {
            @Override
            public double eval(List<Double> parameters) {
                boolean isTrue = !(parameters.get(0)==0);
                return isTrue ? parameters.get(1) : parameters.get(2);
            }
        });

        addFunction(new Function("RANDOM", 0) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.random();
            }
        });
        addFunction(new Function("SIN", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.sin(Math.toRadians(parameters.get(0)
                        .doubleValue()));
            }
        });
        addFunction(new Function("COS", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.cos(Math.toRadians(parameters.get(0)
                        .doubleValue()));
            }
        });
        addFunction(new Function("TAN", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.tan(Math.toRadians(parameters.get(0)
                        .doubleValue()));
            }
        });
        addFunction(new Function("ASIN", 1) { // added by av
            @Override
            public double eval(List<Double> parameters) {
                return Math.toDegrees(Math.asin(parameters.get(0)
                        .doubleValue()));
            }
        });
        addFunction(new Function("ACOS", 1) { // added by av
            @Override
            public double eval(List<Double> parameters) {
                return Math.toDegrees(Math.acos(parameters.get(0)
                        .doubleValue()));
            }
        });
        addFunction(new Function("ATAN", 1) { // added by av
            @Override
            public double eval(List<Double> parameters) {
                return Math.toDegrees(Math.atan(parameters.get(0)
                        .doubleValue()));
            }
        });
        addFunction(new Function("SINH", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.sinh(parameters.get(0).doubleValue());
            }
        });
        addFunction(new Function("COSH", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.cosh(parameters.get(0).doubleValue());
            }
        });
        addFunction(new Function("TANH", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.tanh(parameters.get(0).doubleValue());
            }
        });
        addFunction(new Function("RAD", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return  Math.toRadians(parameters.get(0).doubleValue());
            }
        });
        addFunction(new Function("DEG", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return  Math.toDegrees(parameters.get(0).doubleValue());

            }
        });
        addFunction(new Function("MAX", 2) {
            @Override
            public double eval(List<Double> parameters) {
                double v1 = parameters.get(0);
                double v2 = parameters.get(1);
                return v1>v2 ? v1 : v2;
            }
        });
        addFunction(new Function("MIN", 2) {
            @Override
            public double eval(List<Double> parameters) {
                double v1 = parameters.get(0);
                double v2 = parameters.get(1);
                return v1<v2 ? v1 : v2;
            }
        });
        addFunction(new Function("ABS", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return Math.abs(parameters.get(0));
            }
        });
        addFunction(new Function("LOG", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return  Math.log(parameters.get(0).doubleValue());
            }
        });
        addFunction(new Function("LOG10", 1) {
            @Override
            public double eval(List<Double> parameters) {
                return  Math.log10(parameters.get(0).doubleValue());
            }
        });
        /*
        addFunction(new Function("ROUND", 2) {
            @Override
            public double eval(List<Double> parameters) {
                double toRound = parameters.get(0);
                int precision = parameters.get(1).intValue();
                return toRound.setScale(precision, mc.getRoundingMode());
            }
        });
        addFunction(new Function("FLOOR", 1) {
            @Override
            public double eval(List<Double> parameters) {
                double toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.FLOOR);
            }
        });
        addFunction(new Function("CEILING", 1) {
            @Override
            public double eval(List<Double> parameters) {
                double toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.CEILING);
            }
        });
        */
        addFunction(new Function("SQRT", 1) {
            @Override
            public double eval(List<Double> parameters) {
				/*
				 * From The Java Programmers Guide To numerical Computing
				 * (Ronald Mak, 2003)
				 */
                return Math.sqrt(parameters.get(0));

            }
        });

        variables.put("PI", Math.PI);
        variables.put("TRUE", 1.0);
        variables.put("FALSE", 0.0);

    }

    /**
     * Is the string a number?
     *
     * @param st
     *            The string.
     * @return <code>true</code>, if the input string is a number.
     */
    private boolean isNumber(String st) {
        if (st.charAt(0) == minusSign && st.length() == 1)
            return false;
        for (char ch : st.toCharArray()) {
            if (!Character.isDigit(ch) && ch != minusSign
                    && ch != decimalSeparator)
                return false;
        }
        return true;
    }

    /**
     * Implementation of the <i>Shunting Yard</i> algorithm to transform an
     * infix expression to a RPN expression.
     *
     * @param expression
     *            The input expression in infx.
     * @return A RPN representation of the expression, with each token as a list
     *         member.
     */
    private List<String> shuntingYard(String expression) {
        List<String> outputQueue = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();

        Tokenizer tokenizer = new Tokenizer(expression);

        String lastFunction = null;
        String previousToken = null;
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (isNumber(token)) {
                outputQueue.add(token);
            } else if (variables.containsKey(token)) {
                outputQueue.add(token);
            } else if (functions.containsKey(token.toUpperCase())) {
                stack.push(token);
                lastFunction = token;
            } else if (Character.isLetter(token.charAt(0))) {
                stack.push(token);
            } else if (",".equals(token)) {
                while (!stack.isEmpty() && !"(".equals(stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new ExpressionException("Parse error for function '"
                            + lastFunction + "'");
                }
            } else if (operators.containsKey(token)) {
                Operator o1 = operators.get(token);
                String token2 = stack.isEmpty() ? null : stack.peek();
                while (operators.containsKey(token2)
                        && ((o1.isLeftAssoc() && o1.getPrecedence() <= operators
                        .get(token2).getPrecedence()) || (o1
                        .getPrecedence() < operators.get(token2)
                        .getPrecedence()))) {
                    outputQueue.add(stack.pop());
                    token2 = stack.isEmpty() ? null : stack.peek();
                }
                stack.push(token);
            } else if ("(".equals(token)) {
                if (previousToken != null) {
                    if (isNumber(previousToken)) {
                        throw new ExpressionException("Missing operator at character position " + tokenizer.getPos());
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
                        && functions.containsKey(stack.peek().toUpperCase())) {
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
            if (!operators.containsKey(element)) {
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
    public double eval() {

        Stack<Double> stack = new Stack<Double>();

        for (String token : getRPN()) {
            if (operators.containsKey(token)) {
                double v1 = stack.pop();
                double v2 = stack.pop();
                stack.push(operators.get(token).eval(v2, v1));
            } else if (variables.containsKey(token)) {
                stack.push(variables.get(token));
            } else if (functions.containsKey(token.toUpperCase())) {
                Function f = functions.get(token.toUpperCase());
                ArrayList<Double> p = new ArrayList<Double>(
                        f.getNumParams());
                for (int i = 0; i < f.numParams; i++) {
                    p.add(0,stack.pop());
                }
                double fResult = f.eval(p);
                stack.push(fResult);
            } else {
                stack.push(new Double (token));
            }
        }
        return stack.pop();
    }



    /**
     * Adds an operator to the list of supported operators.
     *
     * @param operator
     *            The operator to add.
     * @return The previous operator with that name, or <code>null</code> if
     *         there was none.
     */
    public Operator addOperator(Operator operator) {
        return operators.put(operator.getOper(), operator);
    }

    /**
     * Adds a function to the list of supported functions
     *
     * @param function
     *            The function to add.
     * @return The previous operator with that name, or <code>null</code> if
     *         there was none.
     */
    public Function addFunction(Function function) {
        return functions.put(function.getName(), function);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable name.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariable(String variable, double value) {
        variables.put(variable, value);
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariable(String variable, String value) {
        if (isNumber(value))
            variables.put(variable, new Double(value));
        else {
            expression = expression.replaceAll("\\b" + variable + "\\b", "(" + value + ")");
            rpn = null;
        }
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, double value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression and(String variable, String value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression and(String variable, double value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, String value) {
        return setVariable(variable, value);
    }

    /**
     * Get an iterator for this expression, allows iterating over an expression
     * token by token.
     *
     * @return A new iterator instance for this expression.
     */
    public Iterator<String> getExpressionTokenizer() {
        return new Tokenizer(this.expression);
    }

    /**
     * Cached access to the RPN notation of this expression, ensures only one
     * calculation of the RPN per expression instance. If no cached instance
     * exists, a new one will be created and put to the cache.
     *
     * @return The cached RPN instance.
     */
    private List<String> getRPN() {
        if (rpn == null) {
            rpn = shuntingYard(this.expression);
        }
        return rpn;
    }

    /**
     * Get a string representation of the RPN (Reverse Polish Notation) for this
     * expression.
     *
     * @return A string with the RPN representation for this expression.
     */
    public String toRPN() {
        String result = new String();
        for (String st : getRPN()) {
            result = result.isEmpty() ? result : result + " ";
            result += st;
        }
        return result;
    }

}