package org.kevoree.modeling.ast;

public class MModelAttribute {

    private String name;
    private String type;
    private double precision = -1;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public MModelAttribute(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public MModelAttribute clone() {
        MModelAttribute cloned = new MModelAttribute(this.name, this.type);
        cloned.setIndex(index);
        cloned.precision = precision;
        return cloned;
    }

}
