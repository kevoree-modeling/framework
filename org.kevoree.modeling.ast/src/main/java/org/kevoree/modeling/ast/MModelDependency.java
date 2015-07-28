package org.kevoree.modeling.ast;

public class MModelDependency {

    private String name;
    private MModelClass type;
    private Integer index = -1;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public MModelDependency(String name, MModelClass type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public MModelClass getType() {
        return type;
    }

    public MModelDependency clone() {
        return new MModelDependency(this.name, this.type);
    }
    
}
