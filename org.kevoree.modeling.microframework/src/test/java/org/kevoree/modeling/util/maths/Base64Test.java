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
        Assert.assertEquals(KConfig.BEGINNING_OF_TIME, Base64.decode(Base64.encode(KConfig.BEGINNING_OF_TIME)));
    }


    @Test
    public void endOfTimeEncodingTest() {
        Assert.assertEquals(KConfig.END_OF_TIME, Base64.decode(Base64.encode(KConfig.END_OF_TIME)));
    }



    @Test
    public void nullEncodingTest() {
        Assert.assertEquals(KConfig.NULL_LONG, Base64.decode(Base64.encode(KConfig.NULL_LONG)));
    }


    @Test
    public void zeroEncodingTest() {
        Assert.assertEquals(0, Base64.decode(Base64.encode(0)));
    }

    @Test
    public void oneEncodingTest() {
        Assert.assertEquals(1, Base64.decode(Base64.encode(1)));
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
