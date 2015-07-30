package org.kevoree.modeling.ast;

public class MModelInput {

    private String name;
    private String extractor;
    protected int index = -1;

    public Integer getIndex() {
        return index;
    }

    public MModelInput(String name, String extractor, int index) {
        this.name = name;
        this.extractor = extractor;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getExtractor() {
        return extractor;
    }

    public MModelInput clone() {
        return new MModelInput(this.name, this.extractor, this.index);
    }

}
