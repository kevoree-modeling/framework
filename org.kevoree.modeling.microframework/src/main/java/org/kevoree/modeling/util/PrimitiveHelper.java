package org.kevoree.modeling.util;

public class PrimitiveHelper {

    /**
     * @native ts
     * return src.slice(0, prefix.length) == prefix
     */
    public static boolean startsWith(String src, String prefix) {
        return src.startsWith(prefix);
    }

    /**
     * @native ts
     * return src.slice(-prefix.length) == prefix;
     */
    public static boolean endsWith(String src, String prefix) {
        return src.endsWith(prefix);
    }


    /**
     * @native ts
     * if (regex == null) { return false; } else { var m = src.match(regex); return m != null && m.length > 0; }
     */
    public static boolean matches(String src, String regex) {
        return src.matches(regex);
    }

    /**
     * @native ts
     * return src === other
     */
    public static boolean equals(String src, String other) {
        return src.equals(other);
    }

    /**
     * @native ts
     * return parseInt(val);
     */
    public static int parseInt(String val) {
        return Integer.parseInt(val);
    }

    // http://stackoverflow.com/questions/21278234/does-parsedouble-exist-in-javascript

    /**
     * @native ts
     * return +val;
     */
    public static long parseLong(String val) {
        return Long.parseLong(val);
    }

    /**
     * @native ts
     * return parseFloat(val);
     */
    public static double parseDouble(String val) {
        return Double.parseDouble(val);
    }

    /**
     * @native ts
     * return +val;
     */
    public static short parseShort(String val) {
        return Short.parseShort(val);
    }

    /**
     * @native ts
     * return val === "true";
     */
    public static boolean parseBoolean(String val) {
        return Boolean.parseBoolean(val);
    }

    public static short SHORT_MIN_VALUE() {
        return -0x8000;
    }

    public static short SHORT_MAX_VALUE() {
        return 0x7FFF;
    }

    /**
     * @native ts
     * return isNaN(val);
     */
    public static boolean isNaN(double val) {
        return Double.isNaN(val);
    }


    /**
     * @native ts
     * return Number.MIN_VALUE;
     */
    public static double DOUBLE_MIN_VALUE() {
        return Double.MIN_VALUE;
    }

    /**
     * @native ts
     * return Number.MAX_VALUE;
     */
    public static double DOUBLE_MAX_VALUE() {
        return Double.MAX_VALUE;
    }

    /**
     * @native ts
     * var hash = 0;
     * if (target.length == 0) return hash;
     * for (var i = 0; i < target.length; i++) {
     * var char = target.charCodeAt(i);
     * hash = ((hash << 5) - hash) + char;
     * hash = hash & hash; // Convert to 32bit integer
     * }
     * return hash;
     */
    public static int stringHash(String target) {
        return target.hashCode();
    }

}
