package org.kevoree.modeling.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by duke on 11/21/14.
 */
public class MModel {

    private HashMap<String, MModelClassifier> classifiers = new HashMap<>();

    private Integer currentIndex = 0;

    public Collection<MModelClassifier> getClassifiers() {
        return classifiers.values();
    }

    public MModelClassifier get(String fqn) {
        return classifiers.get(fqn);
    }

    public void addClassifier(MModelClassifier classifier) {
        if (classifier instanceof MModelClass) {
            classifier.setIndex(currentIndex);
            currentIndex = currentIndex + 1;
        }
        classifiers.put(classifier.getFqn(), classifier);
    }

    public Collection<MModelClass> getClasses() {
        ArrayList<MModelClass> classes = new ArrayList<MModelClass>();
        for (MModelClassifier cls : classifiers.values()) {
            if (cls instanceof MModelClass) {
                classes.add((MModelClass) cls);
            }
        }
        return classes;
    }
}
