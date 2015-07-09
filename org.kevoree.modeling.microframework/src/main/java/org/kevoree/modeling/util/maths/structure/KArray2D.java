package org.kevoree.modeling.util.maths.structure;

public interface KArray2D {

    int nbRaws();

    int nbColumns();

    double get(int rawIndex, int columnIndex);

    double set(int rawIndex, int columnIndex, double value);

    double add(int rawIndex, int columnIndex, double value);

}
