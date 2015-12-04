package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.util.maths.structure.KArray2D;

public class Array2D implements KArray2D {

    private int _nbRows;
    private int _nbColumns;

    private int _offset;

    private int _segmentIndex;

    private KObjectChunk _segment;

    private KMetaClass _metaClass;

    public Array2D(int p_nbRows, int p_nbColumns, int p_offset, int p_segmentIndex, KObjectChunk p_segment, KMetaClass p_metaClass) {
        this._nbRows = p_nbRows;
        this._nbColumns = p_nbColumns;
        this._offset = p_offset;
        this._segment = p_segment;
        this._segmentIndex = p_segmentIndex;
        this._metaClass = p_metaClass;
    }

    @Override
    public int rows() {
        return this._nbRows;
    }

    @Override
    public int columns() {
        return this._nbColumns;
    }

    private int getIndex(int p_rowIndex, int p_columnIndex) {
       //Column-major for blas library
       return this._offset + p_rowIndex + (this._nbRows* p_columnIndex);
        //row-major: this._offset + (p_rowIndex * this._nbColumns) + p_columnIndex

    }

    @Override
    public double get(int p_rowIndex, int p_columnIndex) {
        return this._segment.getDoubleArrayElem(this._segmentIndex,getIndex(p_rowIndex,p_columnIndex) , this._metaClass);
    }

    @Override
    public double set(int p_rowIndex, int p_columnIndex, double value) {
        this._segment.setDoubleArrayElem(this._segmentIndex,getIndex(p_rowIndex,p_columnIndex), value, this._metaClass);
        return value;
    }

    @Override
    public double add(int rowIndex, int columnIndex, double value) {
        return set(rowIndex, columnIndex, get(rowIndex, columnIndex) + value);
    }

    @Override
    public void setAll(double value) {
        for (int i = 0; i < _nbColumns * _nbRows; i++) {
            this._segment.setDoubleArrayElem(this._segmentIndex, this._offset + i, value, this._metaClass);
        }
    }

    @Override
    public void addRow(int rowindex, int numRow) {
//toDo
    }

    @Override
    public void addCol(int colIndex, int numCol) {
//toDo
    }

    @Override
    public KArray2D clone() {
        NativeArray2D cloned = new NativeArray2D(_nbRows, _nbColumns);
        cloned.setData(this.data());
        return cloned;
    }

    @Override
    public double[] data() {
        //TODO manage offset
        return this._segment.getDoubleArray(this._segmentIndex, this._metaClass);
    }

    @Override
    public void setData(double[] p_data) {
        for (int i = 0; i < p_data.length; i++) {
            setAtIndex(i, p_data[i]);
        }
    }

    @Override
    public double getAtIndex(int index) {
        return this._segment.getDoubleArrayElem(this._segmentIndex, this._offset + index, this._metaClass);
    }

    @Override
    public double setAtIndex(int index, double value) {
        this._segment.setDoubleArrayElem(this._segmentIndex, this._offset + index, value, this._metaClass);
        return value;
    }

    @Override
    public double addAtIndex(int index, double value) {
        return this.setAtIndex(index, getAtIndex(index) + value);
    }

}
