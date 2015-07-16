package org.kevoree.modeling.util.maths;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

import java.util.Arrays;

public class Base64Test {

    @Test
    public void beginingOfTimeEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(KConfig.BEGINNING_OF_TIME);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(KConfig.BEGINNING_OF_TIME + " -> " + enc + " -> " + dec);
        Assert.assertEquals(KConfig.BEGINNING_OF_TIME, dec);
    }


    @Test
    public void endOfTimeEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(KConfig.END_OF_TIME);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(KConfig.END_OF_TIME + " -> " + enc + " -> " + dec);
        Assert.assertEquals(KConfig.END_OF_TIME, dec);
    }


    @Test
    public void nullEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(KConfig.NULL_LONG);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(KConfig.NULL_LONG + " -> " + enc + " -> " + dec);
        Assert.assertEquals(KConfig.NULL_LONG, dec);
    }


    @Test
    public void zeroEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(0);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(0 + " -> " + enc + " -> " + dec);
        Assert.assertEquals(0, dec);
    }

    @Test
    public void oneEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(1);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(1 + " -> " + enc + " -> " + dec);
        Assert.assertEquals(1, dec);
    }

    @Test
    public void minIntEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeInt(0x80000000);
        //System.out.println("Decode");
        long dec = Base64.decodeToInt(enc);
        //System.out.println(0x80000000 + " -> " + enc + " -> " + dec);
        Assert.assertEquals(0x80000000, dec);
    }

    @Test
    public void maxIntEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeInt(0x7fffffff);
        //System.out.println("Decode");
        long dec = Base64.decodeToInt(enc);
        //System.out.println(0x7fffffff + " -> " + enc + " -> " + dec);
        Assert.assertEquals(0x7fffffff, dec);
    }

    @Test
    public void randomBigNumTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(68719476737l);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(0x7fffffff + " -> " + enc + " -> " + dec);
        Assert.assertEquals(68719476737l, dec);
    }


    /** @native ts
     * var enc = Base64.encodeDouble(Number.MAX_VALUE);
     * var dec = Base64.decodeToDouble(enc);
     * org.junit.Assert.assertEquals(Number.MAX_VALUE, dec);
     * */
    @Test
    public void maxDoubleEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeDouble(Double.MAX_VALUE);
        //System.out.println("Decode");
        double dec = Base64.decodeToDouble(enc);
        //System.out.println(Double.toHexString(Double.MAX_VALUE) + " -> " + enc + " -> " + Double.toHexString(dec));
        Assert.assertEquals(Double.MAX_VALUE, dec, 0);
    }

    /** @ignore ts
     * var enc = Base64.encodeDouble(Number.MIN_VALUE);
     * var dec = Base64.decodeToDouble(enc);
     * org.junit.Assert.assertEquals(Number.MIN_VALUE, dec);
     * */
    @Test
    public void minDoubleEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeDouble(Double.MIN_VALUE);
        //System.out.println("Decode");
        double dec = Base64.decodeToDouble(enc);
        System.out.println(Double.MIN_VALUE + " -> " + enc + " -> " + dec);
        Assert.assertEquals(Double.MIN_VALUE, dec, 0);
    }

    /** @ignore ts
     * var enc = Base64.encodeDouble(-Number.MAX_VALUE);
     * var dec = Base64.decodeToDouble(enc);
     * org.junit.Assert.assertEquals(-Number.MAX_VALUE, dec);
     * */
    @Test
    public void negMaxDoubleEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeDouble(-Double.MAX_VALUE);
        //System.out.println("Decode");
        double dec = Base64.decodeToDouble(enc);
        //System.out.println(Double.toHexString(Double.MAX_VALUE) + " -> " + enc + " -> " + Double.toHexString(dec));
        Assert.assertEquals(-Double.MAX_VALUE, dec, 0);
    }

    /** @ignore ts
     * var enc = Base64.encodeDouble(-Number.MIN_VALUE);
     * var dec = Base64.decodeToDouble(enc);
     * org.junit.Assert.assertEquals(-Number.MIN_VALUE, dec);
     * */
    @Test
    public void negMinDoubleEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encodeDouble(-Double.MIN_VALUE);
        //System.out.println("Decode");
        double dec = Base64.decodeToDouble(enc);
        //System.out.println(0x7fffffff + " -> " + enc + " -> " + dec);
        Assert.assertEquals(-Double.MIN_VALUE, dec, 0);
    }

    @Test
    public void boolArrayEncodingTest() {

        for(int i = 0; i < 255; i++) {
            boolean[] tmpArray = new boolean[i];
            for(int j = 0; j < i; j++) {
                tmpArray[j] = Math.random() < 0.5;
            }
            boolArrayInnerTest(tmpArray);
        }
    }

    private void boolArrayInnerTest(boolean[] array) {
        String enc = Base64.encodeBoolArray(array);
        //System.out.println("Decode");
        boolean[] dec = Base64.decodeBoolArray(enc, array.length);
        //System.out.println(0x7fffffff + " -> " + enc + " -> " + dec);
        Assert.assertTrue(array.length == dec.length);
        for(int i = 0; i < array.length; i++) {
           // Assert.assertEquals("\n" + Arrays.toString(array) + "\n -> "+ enc +" -> \n" + Arrays.toString(dec),array[i], dec[i]);
            Assert.assertEquals(array[i], dec[i]);
        }
    }

    /*

        private String printBits(Long val) {
            String toString = Long.toBinaryString(val);
            String res = "";

            for(int i = 0; i < 64-toString.length(); i++) {
                res += "0";
            }
            return res + toString;
        }


        public static void main(String[] args) {
            String res = "";
            int i = 0;
            for(char c= 'A'; c <='Z'; c++) {
                res += "\""+c+"\":" + i + ", "; i++;
            }
            for(char c= 'a'; c <='z'; c++) {
                res += "\""+c+"\":" + i + ", "; i++;
            }
            for(char c= '0'; c <='9'; c++) {
                res += "\""+c+"\":" + i + ", "; i++;
            }
            res += "\"+\":" + i + ", "; i++;
            res += "\"/\":" + i;
            System.out.println(res);
        }


        */


}
