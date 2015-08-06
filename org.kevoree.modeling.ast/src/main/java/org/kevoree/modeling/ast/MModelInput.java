package org.kevoree.modeling.ast;

public class MModelInput {

    private String name;
    private String extractor;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public MModelInput(String name, String extractor) {
        this.name = name;
        this.extractor = extractor;
    }

    public String getName() {
        return name;
    }

    public String getExtractor() {
        return extractor;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public MModelInput clone() {
        MModelInput clone = new MModelInput(this.name, this.extractor);
        clone.index = index;
        return clone;
    }

}
