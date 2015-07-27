package org.kevoree.modeling.ast;

public class MModelAttribute {

    private String name;
    private String type;
    private boolean id = false;
    private double precision = -1;
    private Integer index = -1;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public MModelAttribute(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isId() {
        return id;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public MModelAttribute clone() {
        MModelAttribute cloned = new MModelAttribute(this.name, this.type);
        cloned.id = id;
        cloned.precision = precision;
        return cloned;
    }

}
