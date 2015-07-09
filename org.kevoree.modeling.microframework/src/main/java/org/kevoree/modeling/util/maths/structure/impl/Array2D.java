package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.KArray2D;

public class Array2D implements KArray2D {

    private int _nbRaws;

    private int _nbColumns;

    private int _offset;

    private int _segmentIndex;

    private KMemorySegment _segment;

    public Array2D(int p_nbRaws, int p_nbColumns, int p_offset, KMemorySegment p_segment, int p_segmentIndex) {
        this._nbRaws = p_nbRaws;
        this._nbColumns = p_nbColumns;
        this._offset = p_offset;
        this._segment = p_segment;
        this._segmentIndex = p_segmentIndex;
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
    public double get(int rawIndex, int columnIndex) {
        return 0;
    }

    @Override
    public double set(int rawIndex, int columnIndex) {
        return 0;
    }
}
