package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.abs.AbstractDataType;

public class KPrimitiveTypes {

    public static final int BOOL_ID = -1;
    public static final int STRING_ID = -2;
    public static final int LONG_ID = -3;
    public static final int INT_ID = -4;
    public static final int DOUBLE_ID = -5;
    public static final int CONTINUOUS_ID = -6;

    public static final KType BOOL = new AbstractDataType("BOOL", BOOL_ID);
    public static final KType STRING = new AbstractDataType("STRING", STRING_ID);
    public static final KType LONG = new AbstractDataType("LONG", LONG_ID);
    public static final KType INT = new AbstractDataType("INT", INT_ID);
    public static final KType DOUBLE = new AbstractDataType("DOUBLE", DOUBLE_ID);
    public static final KType CONTINUOUS = new AbstractDataType("CONTINUOUS", CONTINUOUS_ID);

    public static boolean isEnum(int attributeTypeId) {
        return attributeTypeId >= 0;
    }

}