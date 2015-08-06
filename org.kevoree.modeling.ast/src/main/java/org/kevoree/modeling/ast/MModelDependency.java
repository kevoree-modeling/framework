package org.kevoree.modeling.ast;

public class MModelDependency {

    private String name;
    private MModelClass type;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public MModelDependency(String name, MModelClass type) {
        this.name = name;
        this.type = type;

    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public MModelClass getType() {
        return type;
    }

    public MModelDependency clone() {
        MModelDependency clone = new MModelDependency(this.name, this.type);
        clone.index = index;
        return clone;
    }

}
