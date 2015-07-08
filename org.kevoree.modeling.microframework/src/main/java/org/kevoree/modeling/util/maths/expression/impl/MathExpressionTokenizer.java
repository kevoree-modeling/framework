package org.kevoree.modeling.util.maths.expression.impl;

/**
 * Created by duke on 07/07/15.
 */
public class MathExpressionTokenizer {

    /**
     * What character to use for decimal separators.
     */
    private final char decimalSeparator = '.';

    /**
     * What character to use for minus sign (negative values).
     */
    private final char minusSign = '-';

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
     * @param input The expression string.
     */
    public MathExpressionTokenizer(String input) {
        this.input = input.trim();
    }

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
            return '\0';
        }
    }

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
                ch = pos == input.length() ? '\0' : input.charAt(pos);
            }
        } else if (ch == minusSign
                && Character.isDigit(peekNextChar())
                && ("(".equals(previousToken) || ",".equals(previousToken)
                || previousToken == null || MathEntities.getINSTANCE().operators.contains(previousToken))) {
            token.append(minusSign);
            pos++;
            token.append(next());
        } else if (Character.isLetter(ch) || (ch == '_')) {
            while ((Character.isLetter(ch) || Character.isDigit(ch) || (ch == '_')) && (pos < input.length())) {
                token.append(input.charAt(pos++));
                ch = pos == input.length() ? '\0' : input.charAt(pos);
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
                ch = pos == input.length() ? '\0' : input.charAt(pos);
                if (ch == minusSign) {
                    break;
                }
            }
            if (!MathEntities.getINSTANCE().operators.contains(token.toString())) {
                throw new RuntimeException("Unknown operator '" + token + "' at position " + (pos - token.length() + 1));
            }
        }
        return previousToken = token.toString();
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
