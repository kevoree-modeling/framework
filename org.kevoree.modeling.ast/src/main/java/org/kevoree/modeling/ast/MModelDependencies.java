package org.kevoree.modeling.ast;

import java.util.HashMap;
import java.util.Map;

public class MModelDependencies {

    private String name;
    protected int index = -1;

    public Map<String, MModelDependency> dependencies;

    public Integer getIndex() {
        return index;
    }

    public MModelDependencies(String name, Map<String, MModelDependency> deps) {
        if (deps == null) {
            this.dependencies = new HashMap<String, MModelDependency>();
        }
        this.name = name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public MModelDependencies clone() {
        MModelDependencies clone = new MModelDependencies(this.name, this.dependencies);
        clone.index = index;
        return clone;
    }

}
