package org.kevoree.modeling.ast;

import java.util.ArrayList;

public class MModelOperation {

    public ArrayList<MModelOperationParam> inputParams = new ArrayList<MModelOperationParam>();
    public String returnType = null;
    public String name;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public MModelOperation(String name) {
        this.name = name;
    }

    public ArrayList<MModelOperationParam> getInputParams() {
        return inputParams;
    }

    public String getReturnType() {
        return returnType;
    }

    public int returnTypeId = 0;

    public boolean isReturnTypeIsArray() {
        return returnTypeIsArray;
    }

    public void setReturnTypeIsArray(boolean returnTypeIsArray) {
        this.returnTypeIsArray = returnTypeIsArray;
    }

    public boolean returnTypeIsArray = false;

    public int getReturnTypeId() {
        return returnTypeId;
    }

    public String getName() {
        return name;
    }

    public MModelOperation clone() {
        MModelOperation cloned = new MModelOperation(this.name);
        cloned.index = index;
        cloned.inputParams = inputParams;
        cloned.returnType = returnType;
        cloned.returnTypeIsArray = returnTypeIsArray;
        return cloned;
    }

}
