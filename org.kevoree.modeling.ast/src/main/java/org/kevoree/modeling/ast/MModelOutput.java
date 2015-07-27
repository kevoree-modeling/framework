package org.kevoree.modeling.ast;

public class MModelOutput {

    private String name;
    private String type;
    private Integer index = -1;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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

    public MModelOutput clone() {
        return new MModelOutput(this.name, this.type);
    }

}
