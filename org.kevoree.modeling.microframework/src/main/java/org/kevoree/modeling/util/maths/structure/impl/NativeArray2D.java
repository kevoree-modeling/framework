package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.util.maths.structure.KArray2D;

import java.lang.reflect.Array;
import java.util.Arrays;

public class NativeArray2D implements KArray2D {

    private int _nbRows;

    private int _nbColumns;

    private double[] _back;

    public NativeArray2D(int p_nbRows, int p_nbColumns) {
        this._nbRows = p_nbRows;
        this._nbColumns = p_nbColumns;
        this._back = new double[p_nbRows * p_nbColumns];
    }

    private int getIndex(int p_rowIndex, int p_columnIndex) {
        //Column-major for blas library
        return p_rowIndex + (this._nbRows* p_columnIndex);
        //row-major: (p_rowIndex * this._nbColumns) + p_columnIndex
    }

    @Override
    public int rows() {
        return this._nbRows;
    }

    @Override
    public int columns() {
        return this._nbColumns;
    }

    @Override
    public double get(int p_rowIndex, int p_columnIndex) {
        return this._back[getIndex(p_rowIndex,p_columnIndex)];
    }

    @Override
    public double set(int p_rowIndex, int p_columnIndex, double value) {
        this._back[getIndex(p_rowIndex,p_columnIndex)] = value;
        return value;
    }

    @Override
    public double add(int rowIndex, int columnIndex, double value) {
        return set(rowIndex, columnIndex, get(rowIndex, columnIndex) + value);
    }

    @Override
    public void setAll(double value) {
        //Arrays.fill(_back,value);
    }

    @Override
    public void addRow(int rowindex, int numRow) {
//todo
    }

    @Override
    public void addCol(int colIndex, int numCol) {
//todo
    }
    
    @Override
    public KArray2D clone() {
        NativeArray2D newArr = new NativeArray2D(this._nbRows, this._nbColumns);
        System.arraycopy(_back, 0, newArr._back, 0, _nbColumns * _nbRows);
        return newArr;
    }

    @Override
    public double[] data() {
        return this._back;
    }

    @Override
    public void setData(double[] data) {
        this._back = data;
    }

    @Override
    public double getAtIndex(int index) {
        return this._back[index];
    }

    @Override
    public double setAtIndex(int index, double value) {
        this._back[index] = value;
        return value;
    }

    @Override
    public double addAtIndex(int index, double value) {
        this._back[index] += value;
        return this._back[index];
    }

}
