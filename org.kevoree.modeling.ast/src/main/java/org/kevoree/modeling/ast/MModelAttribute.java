package org.kevoree.modeling.ast;

/**
 * Created by gregory.nain on 14/10/2014.
 */
public class MModelAttribute {

    private String name;
    private String type;
    private boolean id = false;
    private double precision = -1;
    private boolean single = true;
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

    public void setSingle(boolean single) {
        this.single = single;
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

    public boolean isSingle() {
        return single;
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
        cloned.single = single;
        return cloned;
    }

}
