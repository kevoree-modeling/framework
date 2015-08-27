package org.kevoree.modeling.util.maths.structure;

public interface KArray1D {

    int size();

    double get(int index);

    double set(int index, double value);

    double add (int index, double value);

    double mult(int index, double value);

    void addAll(double value);

    void multAll(double value);

    void setAll(double value);

    void addElement(int index, int numElem);

    KArray2D clone();
}
