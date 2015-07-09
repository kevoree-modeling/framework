package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.util.maths.structure.KArray1D;

public class Array1D implements KArray1D {

    private int _size;

    private int _offset;

    private int _segmentIndex;

    private KMemorySegment _segment;

    public Array1D(int p_size, int p_offset, int p_segmentIndex, KMemorySegment p_segment) {
        this._size = p_size;
        this._offset = p_offset;
        this._segmentIndex = p_segmentIndex;
        this._segment = p_segment;
    }

    @Override
    public int size() {
        return this._size;
    }

    @Override
    public double get(int index) {
        return 0;
    }

    @Override
    public double set(int index) {
        return 0;
    }

}
