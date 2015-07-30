package org.kevoree.modeling.ast;

public class MModelAttribute {

    private String name;
    private String type;
    private double precision = -1;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public MModelAttribute(String name, String type, int index) {
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

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public MModelAttribute clone() {
        MModelAttribute cloned = new MModelAttribute(this.name, this.type, this.index);
        cloned.precision = precision;
        return cloned;
    }

}
