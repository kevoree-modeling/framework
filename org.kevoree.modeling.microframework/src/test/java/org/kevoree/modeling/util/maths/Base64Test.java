package org.kevoree.modeling.util.maths;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.util.PrimitiveHelper;

public class Base64Test {

    @Test
    public void beginingOfTimeEncodingTest() {
        testLong(KConfig.BEGINNING_OF_TIME);
    }

    @Test
    public void endOfTimeEncodingTest() {
        testLong(KConfig.END_OF_TIME);
    }

    @Test
    public void nullEncodingTest() {
        testLong(KConfig.NULL_LONG);
    }

    @Test
    public void zeroEncodingTest() {
        testLong(0l);
    }

    @Test
    public void oneEncodingTest() {
        testLong(1l);
    }

    @Test
    public void randomBigNumTest() {
        testLong(68719476737l);
    }

    private void testLong(long val) {
        //System.out.println("Encode");
        String enc = Base64.encodeLong(val);
        //System.out.println("Decode");
        long dec = Base64.decodeToLong(enc);
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);

        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeLongToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToLong(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
    }


    @Test
    public void minIntEncodingTest() {
        testInt(0x80000000);
    }

    @Test
    public void maxIntEncodingTest() {
        testInt(0x7fffffff);
    }

    private void testInt(int val) {
        //System.out.println("Encode");
        String enc = Base64.encodeInt(val);
        //System.out.println("Decode");
        int dec = Base64.decodeToInt(enc);
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);

        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeIntToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToInt(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
    }


    /**
     * @native ts
     * this.testDouble(Number.MAX_VALUE);
     */
    @Test
    public void maxDoubleEncodingTest() {
        testDouble(Double.MAX_VALUE);
    }

    /**
     * @native ts
     * this.testDouble(Number.MIN_VALUE);
     */
    @Test
    public void minDoubleEncodingTest() {
        testDouble(PrimitiveHelper.DOUBLE_MIN_VALUE());
    }

    /**
     * @native ts
     * this.testDouble(-Number.MAX_VALUE);
     */
    @Test
    public void negMaxDoubleEncodingTest() {
        testDouble(-PrimitiveHelper.DOUBLE_MAX_VALUE());
    }

    /**
     * @native ts
     * this.testDouble(-Number.MIN_VALUE);
     */
    @Test
    public void negMinDoubleEncodingTest() {
        testDouble(-PrimitiveHelper.DOUBLE_MIN_VALUE());
    }

    @Test
    public void zeroDoubleEncodingTest() {
        testDouble(0);
        testDouble(0.5);
        testDouble(0.75);
        testDouble(0.25);
        testDouble(0.000000000000002);
    }

    /**
     * @native ts
     * var enc = Base64.encodeDouble(val);
     * var dec = Base64.decodeToDouble(enc);
     * org.junit.Assert.assertEquals(val, dec);
     * var buffer = new java.lang.StringBuilder();
     * Base64.encodeDoubleToBuffer(val, buffer);
     * dec = Base64.decodeToDouble(buffer.toString());
     * org.junit.Assert.assertEquals(val, dec);
     */
    private void testDouble(double val) {
        //System.out.println("Encode");
        String enc = Base64.encodeDouble(val);
        //System.out.println("Decode");
        double dec = Base64.decodeToDouble(enc);
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec, 0);

        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeDoubleToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToDouble(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec, 0);
    }


    @Test
    public void boolArrayEncodingTest() {

        for (int i = 0; i < 255; i++) {
            boolean[] tmpArray = new boolean[i];
            for (int j = 0; j < i; j++) {
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
        for (int i = 0; i < array.length; i++) {
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
