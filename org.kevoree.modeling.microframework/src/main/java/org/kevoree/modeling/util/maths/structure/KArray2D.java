package org.kevoree.modeling.util.maths.structure;

public interface KArray2D {

    int nbRows();

    int nbColumns();

    double get(int rowIndex, int columnIndex);

    double set(int rowIndex, int columnIndex, double value);

    double add(int rowIndex, int columnIndex, double value);

    double mult(int rowIndex, int columnIndex, double value);

    void addAll(double value);

    void multAll(double value);

    void setAll(double value);

    void addRow(int rowindex, int numRow);

    void addCol(int colIndex, int numCol);

    KArray2D clone();





}
