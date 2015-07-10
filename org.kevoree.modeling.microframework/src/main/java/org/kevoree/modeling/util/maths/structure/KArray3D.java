package org.kevoree.modeling.util.maths.structure;

public interface KArray3D {

    int nbRows();

    int nbColumns();

    int nbDeeps();

    double get(int rowIndex, int columnIndex, int deepIndex);

    double set(int rowIndex, int columnIndex, int deepIndex, double value);

    double add(int p_rowIndex, int p_columnIndex, int p_deepIndex, double value);
    
}
