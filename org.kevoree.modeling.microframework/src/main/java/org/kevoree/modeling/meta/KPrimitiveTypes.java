package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.abs.AbstractDataType;

public class KPrimitiveTypes {

    public static final KType BOOL = new AbstractDataType("BOOL", false);
    public static final KType STRING = new AbstractDataType("STRING", false);
    public static final KType LONG = new AbstractDataType("LONG", false);
    public static final KType INT = new AbstractDataType("INT", false);
    public static final KType DOUBLE = new AbstractDataType("DOUBLE", false);
    public static final KType CONTINUOUS = new AbstractDataType("CONTINUOUS", false);

}