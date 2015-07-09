package org.kevoree.modeling.util.maths.structure;

public interface KArray3D {

    int nbRaws();

    int nbColumns();

    int nbDeeps();

    double get(int rawIndex, int columnIndex, int deepIndex);

    double set(int rawIndex, int columnIndex, int deepIndex, double value);

    double add(int p_rawIndex, int p_columnIndex, int p_deepIndex, double value);
    
}
