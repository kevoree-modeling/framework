package org.kevoree.modeling.util.maths;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

/**
 * Created by gnain on 10/07/15.
 */
public class Base64Test {

    @Test
    public void beginingOfTimeEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encode(KConfig.BEGINNING_OF_TIME);
        //System.out.println("Decode");
        long dec = Base64.decode(enc);
        //System.out.println(KConfig.BEGINNING_OF_TIME + " -> " + enc + " -> " + dec);
        Assert.assertEquals(KConfig.BEGINNING_OF_TIME, dec);
    }


    @Test
    public void endOfTimeEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encode(KConfig.END_OF_TIME);
        //System.out.println("Decode");
        long dec = Base64.decode(enc);
        //System.out.println(KConfig.END_OF_TIME + " -> " + enc + " -> " + dec);
        Assert.assertEquals(KConfig.END_OF_TIME, dec);
    }



    @Test
    public void nullEncodingTest() {
        //System.out.println("Encode");
        String enc = Base64.encode(KConfig.NULL_LONG);
        //System.out.println("Decode");
        long dec = Base64.decode(enc);
        //System.out.println(KConfig.NULL_LONG + " -> " + enc + " -> " + dec);
        Assert.assertEquals(KConfig.NULL_LONG, dec);
    }


    @Test
    public void zeroEncodingTest() {
        Assert.assertEquals(0, Base64.decode(Base64.encode(0)));
        //System.out.println("Encode");
        String enc = Base64.encode(0);
        //System.out.println("Decode");
        long dec = Base64.decode(enc);
        //System.out.println(0 + " -> " + enc + " -> " + dec);
        Assert.assertEquals(0, dec);
    }

    @Test
    public void oneEncodingTest() {
        Assert.assertEquals(1, Base64.decode(Base64.encode(1)));
        //System.out.println("Encode");
        String enc = Base64.encode(1);
        //System.out.println("Decode");
        long dec = Base64.decode(enc);
        //System.out.println(1 + " -> " + enc + " -> " + dec);
        Assert.assertEquals(1, dec);
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
