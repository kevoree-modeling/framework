package org.kevoree.modeling.ast;

public class MModelRelation {

    private String name;
    private MModelClass type;
    private String opposite = null;
    protected int index = -1;
    private int maxBound = -1;
    private boolean visible = true;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public MModelRelation(String name, MModelClass type) {
        this.name = name;
        this.type = type;
    }

    public int getMaxBound() {
        return maxBound;
    }

    public String getName() {
        return name;
    }

    public MModelClass getType() {
        return type;
    }

    public void setMaxBound(int mb) {
        this.maxBound = mb;
    }

    public String getOpposite() {
        return opposite;
    }

    public void setOpposite(String opposite) {
        this.opposite = opposite;
    }

    public MModelRelation clone() {
        MModelRelation cloned = new MModelRelation(this.name, this.type);
        cloned.index = index;
        cloned.opposite = opposite;
        cloned.maxBound = maxBound;
        cloned.visible = visible;
        return cloned;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
