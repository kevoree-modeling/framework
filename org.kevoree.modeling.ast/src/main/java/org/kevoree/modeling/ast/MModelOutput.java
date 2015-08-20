package org.kevoree.modeling.ast;

public class MModelOutput {

    private String name;
    private String type;
    protected int index = -1;

    public int typeId = 0;

    public Integer getIndex() {
        return index;
    }

    public MModelOutput(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getTypeId(){
        return typeId;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public MModelOutput clone() {
        MModelOutput clone = new MModelOutput(this.name, this.type);
        clone.index = index;
        return clone;
    }

}
