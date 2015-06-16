package org.kevoree.modeling.format.json;

/**
 * @ignore ts
 */
public class Lexer {

    private String bytes;
    private int index = 0;

    public Lexer(String payload) {
        this.bytes = payload;
    }

    public boolean isSpace(Character c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }

    private char nextChar() {
        return bytes.charAt(index++);
    }

    private char peekChar() {
        return bytes.charAt(index);
    }

    private boolean isDone() {
        return index >= bytes.length();
    }

    private boolean isBooleanLetter(char c) {
        return c == 'f' || c == 'a' || c == 'l' || c == 's' || c == 'e' || c == 't' || c == 'r' || c == 'u';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isValueLetter(Character c) {
        return c == '-' || c == '+' || c == '.' || isDigit(c) || isBooleanLetter(c);
    }

    private String _lastValue = null;

    public String lastValue() {
        return this._lastValue;
    }

    public JsonType nextToken() {
        if (isDone()) {
            return JsonType.EOF;
        }
        Character c = nextChar();
        while (!isDone() && isSpace(c)) {
            c = nextChar();
        }
        if ('"' == c) {
            if (!isDone()) {
                c = nextChar();
                StringBuilder currentValue = new StringBuilder();
                while (index < bytes.length() && c != '"') {
                    currentValue.append(c);
                    if (c == '\\' && index < bytes.length()) {
                        c = nextChar();
                        currentValue.append(c);
                    }
                    c = nextChar();
                }
                _lastValue = currentValue.toString();
            }
            return JsonType.VALUE;
        } else if ('{' == c) {
            return JsonType.LEFT_BRACE;
        } else if ('}' == c) {
            return JsonType.RIGHT_BRACE;
        } else if ('[' == c) {
            return JsonType.LEFT_BRACKET;
        } else if (']' == c) {
            return JsonType.RIGHT_BRACKET;
        } else if (':' == c) {
            return JsonType.COLON;
        } else if (',' == c) {
            return JsonType.COMMA;
        } else if (!isDone()) {
            StringBuilder currentValue = new StringBuilder();
            while (isValueLetter(c)) {
                currentValue.append(c);
                if (!isValueLetter(peekChar())) {
                    break;
                } else {
                    c = nextChar();
                }
            }
            _lastValue = currentValue.toString();
            return JsonType.VALUE;
        } else {
            return JsonType.EOF;
        }
    }

}
