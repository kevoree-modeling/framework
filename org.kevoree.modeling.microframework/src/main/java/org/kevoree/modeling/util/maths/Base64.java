package org.kevoree.modeling.util.maths;

import org.kevoree.modeling.KConfig;

import java.math.BigInteger;

/**
 * Created by gnain on 10/07/15.
 */


/**
 * @native ts
 * private static encodeArray = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'];
private static decodeArray = [];
private static initiated = false;
private static init() {
if (!Base64.initiated) {
var i = 0;
for (var c = 'A'.charCodeAt(0); c <= 'Z'.charCodeAt(0); c++) { Base64.decodeArray[String.fromCharCode(c)] = i; i++; }
for (var c = 'a'.charCodeAt(0); c <= 'z'.charCodeAt(0); c++) { Base64.decodeArray[String.fromCharCode(c)] = i; i++; }
for (var c = '0'.charCodeAt(0); c <= '9'.charCodeAt(0); c++) { Base64.decodeArray[String.fromCharCode(c)] = i; i++; }
Base64.decodeArray['+'] = i; i++; Base64.decodeArray['/'] = i;
Base64.initiated = true;
}
}

constructor() {
if (!Base64.initiated) {
Base64.init();
}
}

public static encode(l) {
var result = "";
var tmp = (l < 0) ? (l * -1) + 0x0020000000000000 : l;
for (var i = 48; i >= 0; i -= 6) {
result += Base64.encodeArray[(tmp / (Math.pow(2, i))) & 0x3F];
}
return result;
}

public static decode(s) {
var result = 0;
result += (Base64.decodeArray[s.charAt(0)] & 0x1F) * Math.pow(2, 48);
for (var i = 1; i < s.length; i++) {
result += (Base64.decodeArray[s.charAt(i)] & 0xFF) * Math.pow(2, 48 - (6 * i));
}
if ((Base64.decodeArray[s.charAt(0)] & 0x0020) != 0) {
result *= -1;
}
return result;
}
 */
public class Base64 {

    private final static char[] encodeArray = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private final static int[] decodeArray = new int[123];
    static {
        int i = 0;
        for(char c= 'A'; c <='Z'; c++) { decodeArray[c] = i; i++;}
        for(char c= 'a'; c <='z'; c++) { decodeArray[c] = i; i++;}
        for(char c= '0'; c <='9'; c++) { decodeArray[c] = i; i++;}
        decodeArray['+'] = i; i++;
        decodeArray['/'] = i;
    }

    public static String encode(long l) {
        String result = "";
        long tmp = (l<0?(l*-1)|0x0020000000000000l:l);
       for(int i = 48; i >= 0; i-=6) {
            result += encodeArray[(int) (tmp >> i) & 0x3F];
        }
        return result;
    }

    public static long decode(String s) {
        long result = 0;
        result += ((long)(decodeArray[s.charAt(0)] & 0x1F)) << 48;
        for(int i = 1; i < s.length();i++) {
            result |= ((long)(decodeArray[s.charAt(i)] & 0xFF)) << (48-(6*i));
            //System.out.println("i:" + i + " c:" + s.charAt(i) + " d:" + (decodeArray[s.charAt(i)] & 0xFF) + " dec:" + ((decodeArray[s.charAt(i)] & 0xFF) << (48-(6*i))) + " decIdx:" + (48-(6*i)) + " res:" + result);
        }
        if((decodeArray[s.charAt(0)] & 0x0020) != 0) {
            result *= -1;
        }
        return result;
    }

}
