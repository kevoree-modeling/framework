package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.util.maths.structure.KArray2D;

public class NativeArray2D implements KArray2D {

    private int _nbRows;

    private int _nbColumns;

    private double[] _back;

    public NativeArray2D(int p_nbRows, int p_nbColumns) {
        this._nbRows = p_nbRows;
        this._nbColumns = p_nbColumns;
        this._back = new double[p_nbRows * p_nbColumns];
    }

    @Override
    public int nbRows() {
        return this._nbRows;
    }

    @Override
    public int nbColumns() {
        return this._nbColumns;
    }

    @Override
    public double get(int p_rowIndex, int p_columnIndex) {
        return this._back[(p_rowIndex * this._nbColumns) + p_columnIndex];
    }

    @Override
    public double set(int p_rowIndex, int p_columnIndex, double value) {
        this._back[(p_rowIndex * this._nbColumns) + p_columnIndex] = value;
        return value;
    }

    @Override
    public double add(int rawIndex, int columnIndex, double value) {
        return set(rawIndex, columnIndex, get(rawIndex, columnIndex) + value);
    }
}
