package org.kevoree.modeling.ast;

public class MModelOutput {

    private String name;
    private String type;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public MModelOutput(String name, String type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public MModelOutput clone() {
        return new MModelOutput(this.name, this.type, this.index);
    }

}
