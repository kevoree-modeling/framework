package org.kevoree.modeling.infer;

public interface KInferArray2D {

    int nbRaws();

    int nbColumns();

    double get(int rawIndex, int columnIndex);

    double set(int rawIndex, int columnIndex);

}
