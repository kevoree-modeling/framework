package org.kevoree.modeling.ast;

import java.util.ArrayList;

/**
 * Created by gregory.nain on 20/10/14.
 */
public class MModelOperation {

    public ArrayList<MModelOperationParam> inputParams = new ArrayList<MModelOperationParam>();
    public MModelOperationParam returnParam = null;
    public String name;
    protected Integer index = -1;
    
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

    public MModelOperationParam getReturnParam() {
        return returnParam;
    }

    public String getName() {
        return name;
    }

    public MModelOperation clone() {
        MModelOperation cloned = new MModelOperation(this.name);
        cloned.inputParams = inputParams;
        cloned.returnParam = returnParam;
        return cloned;
    }

}
