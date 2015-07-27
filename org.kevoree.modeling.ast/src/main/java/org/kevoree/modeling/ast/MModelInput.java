package org.kevoree.modeling.ast;

public class MModelInput {

    private String name;
    private String extractor;
    private Integer index = -1;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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

    public MModelInput clone() {
        return new MModelInput(this.name, this.extractor);
    }

}
