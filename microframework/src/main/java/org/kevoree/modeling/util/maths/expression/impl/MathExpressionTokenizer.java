package org.kevoree.modeling.util.maths.expression.impl;

import org.kevoree.modeling.util.PrimitiveHelper;

public class MathExpressionTokenizer {

    private int pos = 0;
    private String input;
    private String previousToken;

    public MathExpressionTokenizer(String input) {
        this.input = input.trim();
    }

    public boolean hasNext() {
        return (pos < input.length());
    }

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
        while (MathExpressionEngine.isWhitespace(ch) && pos < input.length()) {
            ch = input.charAt(++pos);
        }
        if (MathExpressionEngine.isDigit(ch)) {
            while ((MathExpressionEngine.isDigit(ch) || ch == MathExpressionEngine.decimalSeparator) && (pos < input.length())) {
                token.append(input.charAt(pos++));
                ch = pos == input.length() ? '\0' : input.charAt(pos);
            }
        } else if (ch == MathExpressionEngine.minusSign && MathExpressionEngine.isDigit(peekNextChar()) && (PrimitiveHelper.equals("(",previousToken) || PrimitiveHelper.equals(",",previousToken) || previousToken == null || MathEntities.getINSTANCE().operators.contains(previousToken))) {
            token.append(MathExpressionEngine.minusSign);
            pos++;
            token.append(next());
        } else if (MathExpressionEngine.isLetter(ch) || (ch == '_')) {
            while ((MathExpressionEngine.isLetter(ch) || MathExpressionEngine.isDigit(ch) || (ch == '_')) && (pos < input.length())) {
                token.append(input.charAt(pos++));
                ch = pos == input.length() ? '\0' : input.charAt(pos);
            }
        } else if (ch == '(' || ch == ')' || ch == ',') {
            token.append(ch);
            pos++;
        } else {
            while (!MathExpressionEngine.isLetter(ch) && !MathExpressionEngine.isDigit(ch) && ch != '_' && !MathExpressionEngine.isWhitespace(ch) && ch != '(' && ch != ')' && ch != ',' && (pos < input.length())) {
                token.append(input.charAt(pos));
                pos++;
                ch = pos == input.length() ? '\0' : input.charAt(pos);
                if (ch == MathExpressionEngine.minusSign) {
                    break;
                }
            }
            if (!MathEntities.getINSTANCE().operators.contains(token.toString())) {
                throw new RuntimeException("Unknown operator '" + token + "' at position " + (pos - token.length() + 1));
            }
        }
        return previousToken = token.toString();
    }

    public int getPos() {
        return pos;
    }

}
