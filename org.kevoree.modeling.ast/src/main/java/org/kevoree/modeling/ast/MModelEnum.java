package org.kevoree.modeling.ast;

import java.util.*;

public class MModelEnum extends MModelClassifier {

    private SortedSet<String> litterals = new TreeSet<>();

    public MModelEnum(String name) {
        this.name = name;
    }

    public void addLitteral(String lit) {
        litterals.add(lit);
    }

    public SortedSet<String> getLitterals() {
        return litterals;
    }


}
