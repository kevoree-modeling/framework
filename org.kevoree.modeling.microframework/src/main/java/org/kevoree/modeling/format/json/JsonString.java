package org.kevoree.modeling.format.json;

public class JsonString {

    /*
     * @ignore ts
     */
    private final static Character ESCAPE_CHAR = '\\';

    /*
     * @ignore ts
     */
    public static void encodeBuffer(StringBuilder buffer, String chain) {
        if (chain == null) {
            return;
        }
        int i = 0;
        while (i < chain.length()) {
            Character ch = chain.charAt(i);
            if (ch == '"') {
                buffer.append(ESCAPE_CHAR);
                buffer.append('"');
            } else if (ch == ESCAPE_CHAR) {
                buffer.append(ESCAPE_CHAR);
                buffer.append(ESCAPE_CHAR);
            } else if (ch == '\n') {
                buffer.append(ESCAPE_CHAR);
                buffer.append('n');
            } else if (ch == '\r') {
                buffer.append(ESCAPE_CHAR);
                buffer.append('r');
            } else if (ch == '\t') {
                buffer.append(ESCAPE_CHAR);
                buffer.append('t');
            } else if (ch == '\u2028') {
                buffer.append(ESCAPE_CHAR);
                buffer.append('u');
                buffer.append('2');
                buffer.append('0');
                buffer.append('2');
                buffer.append('8');
            } else if (ch == '\u2029') {
                buffer.append(ESCAPE_CHAR);
                buffer.append('u');
                buffer.append('2');
                buffer.append('0');
                buffer.append('2');
                buffer.append('9');
            } else {
                buffer.append(ch);
            }
            i = i + 1;
        }
    }

    /*
     * @native ts
     * return JSON.stringify(p_chain);
     */
    public static String encode(String p_chain) {
        StringBuilder sb = new StringBuilder();
        encodeBuffer(sb, p_chain);
        return sb.toString();
    }

    /*
     * @native ts
     * return unescape(p_src);
     */
    public static String unescape(String p_src) {
        if (p_src == null) {
            return null;
        }
        if (p_src.length() == 0) {
            return p_src;
        }
        StringBuilder builder = null;
        int i = 0;
        while (i < p_src.length()) {
            Character current = p_src.charAt(i);
            if (current == ESCAPE_CHAR) {
                if (builder == null) {
                    builder = new StringBuilder();
                    builder.append(p_src.substring(0, i));
                }
                i++;
                Character current2 = p_src.charAt(i);
                switch (current2) {
                    case '"':
                        builder.append('\"');
                        break;
                    case '\\':
                        builder.append(current2);
                        break;
                    case '/':
                        builder.append(current2);
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case '{':
                        builder.append("\\{");
                        break;
                    case '}':
                        builder.append("\\}");
                        break;
                    case '[':
                        builder.append("\\[");
                        break;
                    case ']':
                        builder.append("\\]");
                        break;
                    case ',':
                        builder.append("\\,");
                        break;
                }

            } else {
                if (builder != null) {
                    builder = builder.append(current);
                }
            }
            i++;
        }
        if (builder != null) {
            return builder.toString();
        } else {
            return p_src;
        }
    }
}
