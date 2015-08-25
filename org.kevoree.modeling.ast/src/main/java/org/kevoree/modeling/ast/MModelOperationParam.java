package org.kevoree.modeling.ast;

public class MModelOperationParam {
    public String name;
    public String type;
    public int typeId;

    public boolean isArray() {
        return isArray;
    }

    public boolean isArray = false;


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getTypeId() {
        return typeId;
    }
}
