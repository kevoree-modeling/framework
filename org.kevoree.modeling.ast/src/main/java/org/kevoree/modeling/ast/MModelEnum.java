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

    public String getFqn() {
        return (pack != null ? pack + "." + name : name);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Enum[ ");
        int i = 0;
        for (String s : litterals) {
            if (i != 0) {
                sb.append(" , ");
            }
            sb.append(s);
            i++;
        }
        sb.append(" ]\n");
        return sb.toString();
    }

}
