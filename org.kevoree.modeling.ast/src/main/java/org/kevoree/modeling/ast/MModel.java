package org.kevoree.modeling.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by duke on 11/21/14.
 */
public class MModel {

    private HashMap<String, MModelClassifier> classifiers = new HashMap<>();

    private Integer classIndex = 0;
    private Integer enumIndex = 0;

    public Collection<MModelClassifier> getClassifiers() {
        return classifiers.values();
    }

    public MModelClassifier get(String fqn) {
        return classifiers.get(fqn);
    }

    public void addClassifier(MModelClassifier classifier) {
        if (classifier instanceof MModelClass) {
            classifier.setIndex(classIndex);
            classIndex = classIndex + 1;
        } else if (classifier instanceof MModelEnum) {
            classifier.setIndex(enumIndex);
            enumIndex = enumIndex + 1;
        } else {

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
