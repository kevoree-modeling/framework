package org.kevoree.modeling.ast;

public class MMTypeHelper {

    public static boolean isPrimitiveTYpe(String nn) {
        if (nn.equals("Bool")) {
            return true;
        }
        if (nn.equals("String")) {
            return true;
        }
        if (nn.equals("Long")) {
            return true;
        }
        if (nn.equals("Int")) {
            return true;
        }
        if (nn.equals("Double")) {
            return true;
        }
        if (nn.equals("Continuous")) {
            return true;
        }
        return false;
    }

    public static int toId(String nn) {
        if (nn.equals("Bool")) {
            return -1;
        }
        if (nn.equals("String")) {
            return -2;
        }
        if (nn.equals("Long")) {
            return -3;
        }
        if (nn.equals("Int")) {
            return -4;
        }
        if (nn.equals("Double")) {
            return -5;
        }
        if (nn.equals("Continuous")) {
            return -6;
        }
        return 0;
    }

}
