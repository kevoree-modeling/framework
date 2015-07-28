package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.struct.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.util.maths.structure.KArray3D;

public class Array3D implements KArray3D {

    private int _nbrows;

    private int _nbColumns;

    private int _nbDeeps;

    private int _offset;

    private int _segmentIndex;

    private KMemoryChunk _segment;

    private KMetaClass _metaClass;

    public Array3D(int p_nbrows, int p_nbColumns, int p_nbDeeps, int p_offset, int p_segmentIndex, KMemoryChunk p_segment, KMetaClass p_metaClass) {
        this._nbrows = p_nbrows;
        this._nbColumns = p_nbColumns;
        this._nbDeeps = p_nbDeeps;
        this._offset = p_offset;
        this._segmentIndex = p_segmentIndex;
        this._segment = p_segment;
        this._metaClass = p_metaClass;
    }

    @Override
    public int nbRows() {
        return this._nbrows;
    }

    @Override
    public int nbColumns() {
        return this._nbColumns;
    }

    @Override
    public int nbDeeps() {
        return this._nbDeeps;
    }

    @Override
    public double get(int p_rowIndex, int p_columnIndex, int p_deepIndex) {
        return this._segment.getDoubleArrayElem(this._segmentIndex, this._offset + p_rowIndex * (_nbColumns * _nbDeeps) + p_columnIndex * _nbDeeps + p_deepIndex, this._metaClass);
    }

    @Override
    public double set(int p_rowIndex, int p_columnIndex, int p_deepIndex, double p_value) {
        this._segment.setDoubleArrayElem(this._segmentIndex, this._offset + p_rowIndex * (_nbColumns * _nbDeeps) + p_columnIndex * _nbDeeps + p_deepIndex, p_value, this._metaClass);
        return p_value;
    }

    @Override
    public double add(int p_rowIndex, int p_columnIndex, int p_deepIndex, double value) {
        return set(p_rowIndex,p_columnIndex,p_deepIndex,get(p_rowIndex,p_columnIndex,p_deepIndex)+value);
    }
}
