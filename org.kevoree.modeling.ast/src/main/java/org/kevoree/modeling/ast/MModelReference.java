package org.kevoree.modeling.ast;

public class MModelReference {

    private String name;
    private MModelClass type;
    private String opposite = null;
    protected int index = -1;
    private boolean single = true;
    private boolean visible = true;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public MModelReference(String name, MModelClass type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public MModelClass getType() {
        return type;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public String getOpposite() {
        return opposite;
    }

    public void setOpposite(String opposite) {
        this.opposite = opposite;
    }

    public MModelReference clone() {
        MModelReference cloned = new MModelReference(this.name, this.type);
        cloned.index = index;
        cloned.opposite = opposite;
        cloned.single = single;
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
