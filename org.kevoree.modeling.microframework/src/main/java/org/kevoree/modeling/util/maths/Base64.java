package org.kevoree.modeling.util.maths;

/**
 * @native ts
 * private static encodeArray = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'];
 * private static decodeArray = {"A":0, "B":1, "C":2, "D":3, "E":4, "F":5, "G":6, "H":7, "I":8, "J":9, "K":10, "L":11, "M":12, "N":13, "O":14, "P":15, "Q":16, "R":17, "S":18, "T":19, "U":20, "V":21, "W":22, "X":23, "Y":24, "Z":25, "a":26, "b":27, "c":28, "d":29, "e":30, "f":31, "g":32, "h":33, "i":34, "j":35, "k":36, "l":37, "m":38, "n":39, "o":40, "p":41, "q":42, "r":43, "s":44, "t":45, "u":46, "v":47, "w":48, "x":49, "y":50, "z":51, "0":52, "1":53, "2":54, "3":55, "4":56, "5":57, "6":58, "7":59, "8":60, "9":61, "+":62, "/":63};
 *
 * public static encode(l:number) {
 * var result = "";
 * var tmp;
 * if(l < 0) {
 * tmp = ((l * -1) * 2) + 0x1;
 * } else {
 * tmp = l * 2;
 * }
 * for (var i = 48; i >= 0; i -= 6) {
 * if (!(result.equals("") && ((tmp / Math.pow(2, i)) & 0x3F) == 0) || (i == 0)) {
 * result += Base64.encodeArray[(tmp / Math.pow(2, i)) & 0x3F];
 * }
 * }
 * return result;
 * }
 *
 * public static encodeToBuffer(l:number, buffer:java.lang.StringBuilder) {
 * var empty=true;
 * var tmp;
 * if(l < 0) {
 * tmp = ((l * -1) * 2) + 0x1;
 * } else {
 * tmp = l * 2;
 * }
 * for (var i = 48; i >= 0; i -= 6) {
 * if (!(empty && ((tmp / Math.pow(2, i)) & 0x3F) == 0) || (i == 0)) {
 * empty = false;
 * buffer.append(Base64.encodeArray[(tmp / Math.pow(2, i)) & 0x3F]);
 * }
 * }
 * }
 *
 * public static decode(s) {
 * return Base64.decodeWithBounds(s, 0, s.length);
 * }
 *
 * public static decodeWithBounds(s:string, offsetBegin:number, offsetEnd:number) {
 * var result = 0;
 * for (var i = 0; i < (offsetEnd - offsetBegin); i++) {
 * result += (Base64.decodeArray[s.charAt((offsetEnd - 1) - i)] & 0xFF) * Math.pow(2, (6 * i));
 * }
 * if ((result & 0x1) != 0) {
 * result = (result & 0xFFFFFFFFFFFFFFFE) * -1;
 * }
 * result = result / 2;
 * return result;
 * }
 */
public class Base64 {

    private final static char[] encodeArray = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private final static int[] decodeArray = new int[123];

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            decodeArray[c] = i;
            i++;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            decodeArray[c] = i;
            i++;
        }
        for (char c = '0'; c <= '9'; c++) {
            decodeArray[c] = i;
            i++;
        }
        decodeArray['+'] = i;
        i++;
        decodeArray['/'] = i;
    }


    /**
     * Encodes a long in a base-64 string. Sign is encoded on bit 0 of the long => LS bit of the right-most char of the string. 1 for negative; 0 otherwise.
     *
     * @param l the long to encode
     * @return the encoded string
     */
    public static String encode(long l) {
        String result = "";
        long tmp;
        if (l < 0) {
            tmp = ((l * -1) << 1) | 0x0000000000000001l;
        } else {
            tmp = l << 1;
        }
        for (int i = 48; i >= 0; i -= 6) {
            if (!(result.equals("") && ((int) (tmp >> i) & 0x3F) == 0) || (i == 0)) {
                result += encodeArray[(int) (tmp >> i) & 0x3F];
            }
        }
        return result;
    }

    public static void encodeToBuffer(long l, StringBuilder buffer) {
        boolean empty = true;
        long tmp;
        if (l < 0) {
            tmp = ((l * -1) << 1) | 0x0000000000000001l;
        } else {
            tmp = l << 1;
        }
        for (int i = 48; i >= 0; i -= 6) {
            if (!(empty && ((int) (tmp >> i) & 0x3F) == 0) || (i == 0)) {
                empty = false;
                buffer.append(encodeArray[(int) (tmp >> i) & 0x3F]);
            }
        }
    }

    public static long decode(String s) {
        return decodeWithBounds(s, 0, s.length());
    }

    public static long decodeWithBounds(String s, int offsetBegin, int offsetEnd) {
        long result = 0;
        for (int i = 0; i < (offsetEnd - offsetBegin); i++) {
            result |= ((long) (decodeArray[s.charAt((offsetEnd - 1) - i)] & 0xFF)) << (6 * i);
        }
        if ((result & 0x1) != 0) {
            result = (result & 0xFFFFFFFFFFFFFFFEl) * -1;
        }
        result = result >> 1;
        return result;
    }


}
