package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.KArray3D;

public class Array3D implements KArray3D {

    private int _nbRaws;

    private int _nbColumns;

    private int _nbDeeps;

    private int _offset;

    private int _segmentIndex;

    private KMemorySegment _segment;

    public Array3D(int p_nbRaws, int p_nbColumns, int p_nbDeeps, int p_offset, int p_segmentIndex, KMemorySegment p_segment) {
        this._nbRaws = p_nbRaws;
        this._nbColumns = p_nbColumns;
        this._nbDeeps = p_nbDeeps;
        this._offset = p_offset;
        this._segmentIndex = p_segmentIndex;
        this._segment = p_segment;
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
    public double get(int rawIndex, int columnIndex, int deepIndex) {
        return 0;
    }

    @Override
    public double set(int rawIndex, int columnIndex, int deepIndex) {
        return 0;
    }
}
