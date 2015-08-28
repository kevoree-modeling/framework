package org.kevoree.modeling.util.maths.structure;

public interface KArray2D {

    int rows();

    int columns();

    double get(int rowIndex, int columnIndex);

    double set(int rowIndex, int columnIndex, double value);

    double add(int rowIndex, int columnIndex, double value);

    void setAll(double value);

    void addRow(int rowindex, int numRow);

    void addCol(int colIndex, int numCol);


    double getAtIndex(int index);

    double setAtIndex(int index, double value);

    double addAtIndex(int index, double value);

    KArray2D clone();

    double[] data();

    void setData(double[] data);

}
