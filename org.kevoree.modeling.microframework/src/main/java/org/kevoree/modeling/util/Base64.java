package org.kevoree.modeling.util;

/**
 * @native ts
 * private static encodeArray = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'];
 * private static decodeArray = {"A":0, "B":1, "C":2, "D":3, "E":4, "F":5, "G":6, "H":7, "I":8, "J":9, "K":10, "L":11, "M":12, "N":13, "O":14, "P":15, "Q":16, "R":17, "S":18, "T":19, "U":20, "V":21, "W":22, "X":23, "Y":24, "Z":25, "a":26, "b":27, "c":28, "d":29, "e":30, "f":31, "g":32, "h":33, "i":34, "j":35, "k":36, "l":37, "m":38, "n":39, "o":40, "p":41, "q":42, "r":43, "s":44, "t":45, "u":46, "v":47, "w":48, "x":49, "y":50, "z":51, "0":52, "1":53, "2":54, "3":55, "4":56, "5":57, "6":58, "7":59, "8":60, "9":61, "+":62, "/":63};
 * public static encodeLong(l:number) {
 * var result = "";
 * var tmp = l;
 * if(l < 0) {
 * tmp = -tmp;
 * }
 * for (var i = 47; i >= 5; i -= 6) {
 * if (!(result === "" && ((tmp / Math.pow(2, i)) & 0x3F) == 0)) {
 * result += Base64.encodeArray[(tmp / Math.pow(2, i)) & 0x3F];
 * }
 * }
 * result += Base64.encodeArray[(tmp & 0x1F)*2 + (l<0?1:0)];
 * return result;
 * }
 * public static encodeLongToBuffer(l:number, buffer:java.lang.StringBuilder) {
 * var empty=true;
 * var tmp = l;
 * if(l < 0) {
 * tmp = -tmp;
 * }
 * for (var i = 47; i >= 5; i -= 6) {
 * if (!(empty && ((tmp / Math.pow(2, i)) & 0x3F) == 0)) {
 * empty = false;
 * buffer.append(Base64.encodeArray[(tmp / Math.pow(2, i)) & 0x3F]);
 * }
 * }
 * buffer.append(Base64.encodeArray[(tmp & 0x1F)*2 + (l<0?1:0)]);
 * }
 * public static encodeInt(l:number) {
 * var result = "";
 * var tmp = l;
 * if(l < 0) {
 * tmp = -tmp;
 * }
 * for (var i = 29; i >= 5; i -= 6) {
 * if (!(result === "" && ((tmp / Math.pow(2, i)) & 0x3F) == 0)) {
 * result += Base64.encodeArray[(tmp / Math.pow(2, i)) & 0x3F];
 * }
 * }
 * result += Base64.encodeArray[(tmp & 0x1F)*2 + (l<0?1:0)];
 * return result;
 * }
 * public static encodeIntToBuffer(l:number, buffer:java.lang.StringBuilder) {
 * var empty=true;
 * var tmp = l;
 * if(l < 0) {
 * tmp = -tmp;
 * }
 * for (var i = 29; i >= 5; i -= 6) {
 * if (!(empty && ((tmp / Math.pow(2, i)) & 0x3F) == 0)) {
 * empty = false;
 * buffer.append(Base64.encodeArray[(tmp / Math.pow(2, i)) & 0x3F]);
 * }
 * }
 * buffer.append(Base64.encodeArray[(tmp & 0x1F)*2 + (l<0?1:0)]);
 * }
 * public static decodeToLong(s) {
 * return Base64.decodeToLongWithBounds(s, 0, s.length);
 * }
 * public static decodeToLongWithBounds(s:string, offsetBegin:number, offsetEnd:number) {
 * var result = 0;
 * result += (Base64.decodeArray[s.charAt((offsetEnd - 1))] & 0xFF) / 2;
 * for (var i = 1; i < (offsetEnd - offsetBegin); i++) {
 * result += (Base64.decodeArray[s.charAt((offsetEnd - 1) - i)] & 0xFF) * Math.pow(2, (6 * i)-1);
 * }
 * if (((Base64.decodeArray[s.charAt((offsetEnd - 1))] & 0xFF) & 0x1) != 0) {
 * result = -result;
 * }
 * return result;
 * }
 * public static decodeToInt(s) {
 * return Base64.decodeToIntWithBounds(s, 0, s.length);
 * }
 * public static decodeToIntWithBounds(s:string, offsetBegin:number, offsetEnd:number) {
 * var result = 0;
 * result += (Base64.decodeArray[s.charAt((offsetEnd - 1))] & 0xFF) / 2;
 * for (var i = 1; i < (offsetEnd - offsetBegin); i++) {
 * result += (Base64.decodeArray[s.charAt((offsetEnd - 1) - i)] & 0xFF) * Math.pow(2, (6 * i)-1);
 * }
 * if (((Base64.decodeArray[s.charAt((offsetEnd - 1))] & 0xFF) & 0x1) != 0) {
 * result = -result;
 * }
 * return result;
 * }
 * public static encodeDouble(d : number) {
 * var result = "";
 * var floatArr = new Float64Array(1);
 * var bytes = new Uint8Array(floatArr.buffer);
 * floatArr[0] = d;
 * var exponent = ((bytes[7] & 0x7f) << 4 | bytes[6] >> 4) - 0x3ff;
 * var signAndExp = (((bytes[7] >> 7)&0x1) << 11) + (exponent + 1023);
 * //encode sign + exp
 * result += Base64.encodeArray[(signAndExp >> 6) & 0x3F];
 * result += Base64.encodeArray[signAndExp & 0x3F];
 * result += Base64.encodeArray[bytes[6] & 0x0F];
 * result += Base64.encodeArray[(bytes[5] >> 2) & 0x3F];
 * result += Base64.encodeArray[(bytes[5] & 0x3)<<4 | bytes[4] >> 4];
 * result += Base64.encodeArray[(bytes[4] & 0x0F)<<2 | bytes[3] >> 6];
 * result += Base64.encodeArray[(bytes[3] & 0x3F)];
 * result += Base64.encodeArray[(bytes[2] >> 2) & 0x3F];
 * result += Base64.encodeArray[(bytes[2] & 0x3)<<4 | bytes[1] >> 4];
 * result += Base64.encodeArray[(bytes[1] & 0x0F)<<2 | bytes[0] >> 6];
 * result += Base64.encodeArray[(bytes[0] & 0x3F)];
 * var i = result.length-1;
 * while(i >= 3 && result.charAt(i) == 'A') {
 * i--;
 * }
 * return result.substr(0,i+1);
 * }
 * public static encodeDoubleToBuffer(d : number, buffer : java.lang.StringBuilder) {
 * var result = "";
 * var floatArr = new Float64Array(1);
 * var bytes = new Uint8Array(floatArr.buffer);
 * floatArr[0] = d;
 * var exponent = ((bytes[7] & 0x7f) << 4 | bytes[6] >> 4) - 0x3ff;
 * var signAndExp = (((bytes[7] >> 7)&0x1) << 11) + (exponent + 1023);
 * //encode sign + exp
 * result += Base64.encodeArray[(signAndExp >> 6) & 0x3F];
 * result += Base64.encodeArray[signAndExp & 0x3F];
 * result += Base64.encodeArray[bytes[6] & 0x0F];
 * result += Base64.encodeArray[(bytes[5] >> 2) & 0x3F];
 * result += Base64.encodeArray[(bytes[5] & 0x3)<<4 | bytes[4] >> 4];
 * result += Base64.encodeArray[(bytes[4] & 0x0F)<<2 | bytes[3] >> 6];
 * result += Base64.encodeArray[(bytes[3] & 0x3F)];
 * result += Base64.encodeArray[(bytes[2] >> 2) & 0x3F];
 * result += Base64.encodeArray[(bytes[2] & 0x3)<<4 | bytes[1] >> 4];
 * result += Base64.encodeArray[(bytes[1] & 0x0F)<<2 | bytes[0] >> 6];
 * result += Base64.encodeArray[(bytes[0] & 0x3F)];
 * var i = result.length-1;
 * while(i >= 3 && result.charAt(i) == 'A') {
 * i--;
 * }
 * buffer.append(result);
 * }
 * public static decodeToDouble(s:string) {
 * return Base64.decodeToDoubleWithBounds(s, 0, s.length);
 * }
 * public static decodeToDoubleWithBounds(s : string, offsetBegin : number, offsetEnd : number) {
 * var signAndExp = ((Base64.decodeArray[s.charAt(0)] & 0xFF) * Math.pow(2, 6)) + (Base64.decodeArray[s.charAt(1)] & 0xFF);
 * var sign = ((signAndExp & 0x800) != 0 ? -1 : 1);
 * var exp = signAndExp & 0x7FF;
 * //Mantisse
 * var mantissaBits = 0;
 * for (var i = 2; i < (offsetEnd - offsetBegin); i++) {
 * mantissaBits += (Base64.decodeArray[s.charAt(offsetBegin + i)] & 0xFF) * Math.pow(2,(48 - (6 * (i-2))));
 * }
 * return (exp != 0) ? sign * Math.pow(2, exp - 1023) * (1 + (mantissaBits / Math.pow(2, 52))) : sign * Math.pow(2, -1022) * (0 + (mantissaBits / Math.pow(2, 52)));
 * }
 * public static encodeBoolArray(boolArr : Array<boolean>) {
 * var result = "";
 * var tmpVal = 0;
 * for (var i = 0; i < boolArr.length; i++) {
 * tmpVal = tmpVal | ((boolArr[i] ? 1 : 0) * Math.pow(2, i % 6));
 * if (i % 6 == 5 || i == (boolArr.length - 1)) {
 * result += Base64.encodeArray[tmpVal];
 * tmpVal = 0;
 * }
 * }
 * return result;
 * }
 * public static encodeBoolArrayToBuffer(boolArr : Array<boolean>, buffer : java.lang.StringBuilder) {
 * var tmpVal = 0;
 * for (var i = 0; i < boolArr.length; i++) {
 * tmpVal = tmpVal | ((boolArr[i] ? 1 : 0) * Math.pow(2,i % 6));
 * if (i % 6 == 5 || i == boolArr.length - 1) {
 * buffer.append(Base64.encodeArray[tmpVal]);
 * tmpVal = 0;
 * }
 * }
 * }
 * public static decodeBoolArray(s : string, arraySize : number) {
 * return Base64.decodeToBoolArrayWithBounds(s, 0, s.length, arraySize);
 * }
 * public static decodeToBoolArrayWithBounds(s : string, offsetBegin : number, offsetEnd : number, arraySize : number) {
 * var resultTmp = [];
 * for (var i = 0; i < (offsetEnd - offsetBegin); i++) {
 * var bitarray = Base64.decodeArray[s.charAt(offsetBegin + i)] & 0xFF;
 * for (var bit_i = 0; bit_i < 6; bit_i++) {
 * if ((6 * i) + bit_i < arraySize) {
 * resultTmp[(6 * i) + bit_i] = (bitarray & (1 * Math.pow(2, bit_i))) != 0;
 * } else {
 * break;
 * }
 * }
 * }
 * return resultTmp;
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
    public static String encodeLong(long l) {
        String result = "";
        long tmp = l;
        if (l < 0) {
            tmp = -tmp;
        }
        for (int i = 47; i >= 5; i -= 6) {
            if (!(PrimitiveHelper.equals(result, "") && ((int) (tmp >> i) & 0x3F) == 0)) {
                result += encodeArray[(int) (tmp >> i) & 0x3F];
            }
        }
        result += Base64.encodeArray[(int) ((tmp & 0x1F) << 1) + (l < 0 ? 1 : 0)];
        return result;
    }

    public static void encodeLongToBuffer(long l, StringBuilder buffer) {
        boolean empty = true;
        long tmp = l;
        if (l < 0) {
            tmp = -tmp;
        }
        for (int i = 47; i >= 5; i -= 6) {
            if (!(empty && ((int) (tmp >> i) & 0x3F) == 0)) {
                empty = false;
                buffer.append(encodeArray[(int) (tmp >> i) & 0x3F]);
            }
        }
        buffer.append(Base64.encodeArray[(int) ((tmp & 0x1F) << 1) + (l < 0 ? 1 : 0)]);
    }


    /**
     * Encodes a int in a base-64 string. Sign is encoded on bit 0 of the long => LS bit of the right-most char of the string. 1 for negative; 0 otherwise.
     *
     * @param l the int to encode
     * @return the encoded string
     */
    public static String encodeInt(int l) {
        String result = "";
        int tmp = l;
        if (l < 0) {
            tmp = -tmp;
        }
        for (int i = 29; i >= 5; i -= 6) {
            if (!(PrimitiveHelper.equals(result, "") && ((tmp >> i) & 0x3F) == 0)) {
                result += Base64.encodeArray[(tmp >> i) & 0x3F];
            }
        }
        result += Base64.encodeArray[(tmp & 0x1F) * 2 + (l < 0 ? 1 : 0)];
        return result;
    }


    public static void encodeIntToBuffer(int l, StringBuilder buffer) {
        boolean empty = true;
        int tmp = l;
        if (l < 0) {
            tmp = -tmp;
        }
        for (int i = 29; i >= 5; i -= 6) {
            if (!(empty && ((int) (tmp >> i) & 0x3F) == 0)) {
                empty = false;
                buffer.append(encodeArray[(tmp >> i) & 0x3F]);
            }
        }
        buffer.append(Base64.encodeArray[((tmp & 0x1F) << 1) + (l < 0 ? 1 : 0)]);
    }

    public static long decodeToLong(String s) {
        return decodeToLongWithBounds(s, 0, s.length());
    }

    public static long decodeToLongWithBounds(String s, int offsetBegin, int offsetEnd) {
        long result = 0;
        result += (Base64.decodeArray[s.charAt(offsetEnd - 1)] & 0xFF) >> 1;
        for (int i = 1; i < (offsetEnd - offsetBegin); i++) {
            result += ((long) (Base64.decodeArray[s.charAt((offsetEnd - 1) - i)] & 0xFF)) << ((6 * i) - 1);
        }
        if (((Base64.decodeArray[s.charAt(offsetEnd - 1)] & 0xFF) & 0x1) != 0) {
            result = -result;
        }
        return result;
    }

    public static int decodeToInt(String s) {
        return decodeToIntWithBounds(s, 0, s.length());
    }

    public static int decodeToIntWithBounds(String s, int offsetBegin, int offsetEnd) {
        int result = 0;
        result += (Base64.decodeArray[s.charAt(offsetEnd - 1)] & 0xFF) >> 1;
        for (int i = 1; i < (offsetEnd - offsetBegin); i++) {
            result += (Base64.decodeArray[s.charAt((offsetEnd - 1) - i)] & 0xFF) << ((6 * i) - 1);
        }
        if (((Base64.decodeArray[s.charAt(offsetEnd - 1)] & 0xFF) & 0x1) != 0) {
            result = -result;
        }
        return result;
    }


    /**
     * Encodes a boolean array into a Base64 string
     *
     * @param boolArr the array to encode
     * @return the string encoding the array.
     */
    public static String encodeBoolArray(boolean[] boolArr) {
        String result = "";
        int tmpVal = 0;
        for (int i = 0; i < boolArr.length; i++) {
            tmpVal = tmpVal | ((boolArr[i] ? 1 : 0) << i % 6);
            if (i % 6 == 5 || i == boolArr.length - 1) {
                result += Base64.encodeArray[tmpVal];
                tmpVal = 0;
            }
        }
        return result;
    }

    public static void encodeBoolArrayToBuffer(boolean[] boolArr, StringBuilder buffer) {
        int tmpVal = 0;
        for (int i = 0; i < boolArr.length; i++) {
            tmpVal = tmpVal | ((boolArr[i] ? 1 : 0) << i % 6);
            if (i % 6 == 5 || i == boolArr.length - 1) {
                buffer.append(Base64.encodeArray[tmpVal]);
                tmpVal = 0;
            }
        }
    }

    public static boolean[] decodeBoolArray(String s, int arraySize) {
        return decodeToBoolArrayWithBounds(s, 0, s.length(), arraySize);
    }

    public static boolean[] decodeToBoolArrayWithBounds(String s, int offsetBegin, int offsetEnd, int arraySize) {
        boolean[] resultTmp = new boolean[arraySize];
        for (int i = 0; i < (offsetEnd - offsetBegin); i++) {
            int bitarray = Base64.decodeArray[s.charAt(offsetBegin + i)] & 0xFF;
            for (int bit_i = 0; bit_i < 6; bit_i++) {
                if ((6 * i) + bit_i < arraySize) {
                    resultTmp[(6 * i) + bit_i] = (bitarray & (1 << bit_i)) != 0;
                } else {
                    break;
                }
            }
        }
        return resultTmp;
    }

    /**
     * Encodes a double into Base64 string Following the IEEE-754.
     * 2 first chars for sign + exponent; remaining chars on the right for the mantissa.
     * Trailing 'A's (aka 0) are dismissed for compression.
     *
     * @param d the double to encode
     * @return the encoding string
     */
    public static String encodeDouble(double d) {
        String result = "";
        long l = Double.doubleToLongBits(d);
        //encode sign + exp
        result += Base64.encodeArray[(int) (l >> 58) & 0x3F];
        result += Base64.encodeArray[(int) (l >> 52) & 0x3F];
        //encode mantissa
        result += Base64.encodeArray[(int) (l >> 48) & 0x0F];
        for (int i = 42; i >= 0; i -= 6) {
            if (((l >> i) & 0x3F) == 0 && (l & (~(0xFFFFFFFFFFFFFFFFl << i))) == 0) {
                break;
            }
            result += Base64.encodeArray[(int) (l >> i) & 0x3F];
        }
        return result;
    }

    public static void encodeDoubleToBuffer(double d, StringBuilder buffer) {
        long l = Double.doubleToLongBits(d);
        //encode sign + exp
        buffer.append(Base64.encodeArray[(int) (l >> 58) & 0x3F]);
        buffer.append(Base64.encodeArray[(int) (l >> 52) & 0x3F]);
        //encode mantisse
        buffer.append(Base64.encodeArray[(int) (l >> 48) & 0x0F]);
        for (int i = 42; i >= 0; i -= 6) {
            if (((l >> i) & 0x3F) == 0 && (l & (~(0xFFFFFFFFFFFFFFFFl << i))) == 0) {
                return;
            }
            buffer.append(Base64.encodeArray[(int) (l >> i) & 0x3F]);
        }
    }

    public static double decodeToDouble(String s) {
        return decodeToDoubleWithBounds(s, 0, s.length());
    }

    public static double decodeToDoubleWithBounds(String s, int offsetBegin, int offsetEnd) {
        long result = 0;
        //sign + exponent
        result += ((long) Base64.decodeArray[s.charAt(0)] & 0xFF) << 58;
        result += ((long) Base64.decodeArray[s.charAt(1)] & 0xFF) << 52;
        //Mantisse
        for (int i = 2; i < (offsetEnd - offsetBegin); i++) {
            result += ((long) Base64.decodeArray[s.charAt(offsetBegin + i)] & 0xFF) << (48 - (6 * (i - 2)));
        }
        return Double.longBitsToDouble(result);
    }

    /*
    private static String printBits(long val) {
        String toString = Long.toBinaryString(val);
        String res = "";

        for (int i = 0; i < 64 - toString.length(); i++) {
            res += "0";
        }
        return res + toString;
    }
    */


}
