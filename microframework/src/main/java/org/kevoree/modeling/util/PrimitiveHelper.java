package org.kevoree.modeling.util;

import org.kevoree.modeling.KConfig;

/*import java.nio.ByteBuffer;
import java.security.MessageDigest;*/

public class PrimitiveHelper {
    private static final long PRIME1 = 2654435761L;
    private static final long PRIME2 = 2246822519L;
    private static final long PRIME3 = 3266489917L;
    private static final long PRIME4 = 668265263;
    private static final long PRIME5 = 0x165667b1;
    private static final int len = 24;

    public static int tripleHash(long p1, long p2, long p3) {

        long v1 = PRIME5;
        long v2 = v1 * PRIME2 + len;
        long v3 = v2 * PRIME3;
        long v4 = v3 * PRIME4;

        long crc;


        v1 = ((v1 << 13) | (v1 >>> 51)) + p1;
        v2 = ((v2 << 11) | (v2 >>> 53)) + p2;
        v3 = ((v3 << 17) | (v3 >>> 47)) + p3;
        v4 = ((v4 << 19) | (v4 >>> 45));

       v1 +=  ((v1 << 17) | (v1 >>> 47));
        v2 +=  ((v2 << 19) | (v2 >>> 45));
        v3 +=  ((v3 << 13) | (v3 >>> 51));
        v4 +=  ((v4 << 11) | (v4 >>> 53));

        v1 *= PRIME1;
        v2 *= PRIME1;
        v3 *= PRIME1;
        v4 *= PRIME1;

        v1 += p1;
        v2 += p2;
        v3 += p3;
        v4 += PRIME5;

        v1 *= PRIME2;
        v2 *= PRIME2;
        v3 *= PRIME2;
        v4 *= PRIME2;

        v1 +=  ((v1 << 11) | (v1 >>> 53));
        v2 +=  ((v2 << 17) | (v2 >>> 47));
        v3 +=  ((v3 << 19) | (v3 >>> 45));
        v4 +=  ((v4 << 13) | (v4 >>> 51));

        v1 *= PRIME3;
        v2 *= PRIME3;
        v3 *= PRIME3;
        v4 *= PRIME3;

        crc = v1 +  ((v2 << 3) | (v2 >>> 61)) +  ((v3 << 6) | (v3 >>> 58)) +  ((v4 << 9) | (v4 >>> 55));
        crc ^= crc >>> 11;
        crc += (PRIME4 + len) * PRIME1;
        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc ^= crc >>> 13;

        return (int) crc;


        //Polynomial
       /* int result =  (int) (p2 ^ (p2 >>> 32));
        result = 17 * result + (int) (p1 ^ (p1 >>> 32));
        result = 31 * result + (int) (p3 ^ (p3 >>> 32));
        return result;*/

        //Sha1
      /*  MessageDigest md = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES*3);
            buffer.putLong(p1);
            buffer.putLong(p2);
            buffer.putLong(p3);
            md = MessageDigest.getInstance("MD5");


            byte[] res = md.digest(buffer.array());
            return ByteBuffer.wrap(res).getInt();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println("hash failed");
        return 0;*/
    }

    /**
     * @native ts
     * return Math.random() * 1000000
     */
    public static long rand() {
        return (long) (Math.random() * KConfig.END_OF_TIME);
    }

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
     * var charC = target.charCodeAt(i);
     * hash = ((hash << 5) - hash) + charC;
     * hash = hash & hash; // Convert to 32bit integer
     * }
     * return hash;
     */
    public static int stringHash(String target) {
        return target.hashCode();
    }

}
