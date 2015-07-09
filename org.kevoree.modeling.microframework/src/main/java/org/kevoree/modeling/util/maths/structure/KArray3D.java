package org.kevoree.modeling.util.maths.structure;

public interface KArray3D {

    int nbRaws();

    int nbColumns();

    int nbDeeps();

    double get(int rawIndex, int columnIndex, int deepIndex);

    void set(int rawIndex, int columnIndex, int deepIndex, double value);
    
}
