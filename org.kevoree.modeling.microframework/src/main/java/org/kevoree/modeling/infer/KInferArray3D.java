package org.kevoree.modeling.infer;

public interface KInferArray3D {

    int nbRaws();

    int nbColumns();

    int nbDeeps();

    double get(int rawIndex, int columnIndex, int deepIndex);

    double set(int rawIndex, int columnIndex, int deepIndex);
    
}
