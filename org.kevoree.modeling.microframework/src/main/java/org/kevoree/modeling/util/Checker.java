package org.kevoree.modeling.util;

public class Checker {

    /**
     * @native ts
     * return param != undefined && param != null;
     */
    public static boolean isDefined(Object param){
        return param != null;
    }

}
