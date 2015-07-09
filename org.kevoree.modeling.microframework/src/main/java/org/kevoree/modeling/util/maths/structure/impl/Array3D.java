package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.util.maths.structure.KArray3D;

public class Array3D implements KArray3D {

    private int _nbRaws;

    private int _nbColumns;

    private int _nbDeeps;

    private int _offset;

    private int _segmentIndex;

    private KMemorySegment _segment;

    private KMetaClass _metaClass;

    public Array3D(int p_nbRaws, int p_nbColumns, int p_nbDeeps, int p_offset, int p_segmentIndex, KMemorySegment p_segment, KMetaClass p_metaClass) {
        this._nbRaws = p_nbRaws;
        this._nbColumns = p_nbColumns;
        this._nbDeeps = p_nbDeeps;
        this._offset = p_offset;
        this._segmentIndex = p_segmentIndex;
        this._segment = p_segment;
        this._metaClass = p_metaClass;
    }

    @Override
    public int nbRaws() {
        return this._nbRaws;
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
    public double get(int p_rawIndex, int p_columnIndex, int p_deepIndex) {
        return this._segment.getInferElem(this._segmentIndex, this._offset + (p_rawIndex * this._nbColumns * this._nbDeeps) + p_columnIndex + p_deepIndex, this._metaClass);
    }

    @Override
    public double set(int p_rawIndex, int p_columnIndex, int p_deepIndex, double p_value) {
        this._segment.setInferElem(this._segmentIndex, this._offset + (p_rawIndex * this._nbColumns * this._nbDeeps) + p_columnIndex + p_deepIndex, p_value, this._metaClass);
        return p_value;
    }

    @Override
    public double add(int p_rawIndex, int p_columnIndex, int p_deepIndex, double value) {
        return set(p_rawIndex,p_columnIndex,p_deepIndex,get(p_rawIndex,p_columnIndex,p_deepIndex)+value);
    }
}
