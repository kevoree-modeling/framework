package org.kevoree.modeling.util.maths.structure;

public interface KArray2D {

    int nbRows();

    int nbColumns();

    double get(int rowIndex, int columnIndex);

    double set(int rowIndex, int columnIndex, double value);

    double add(int rowIndex, int columnIndex, double value);

}
