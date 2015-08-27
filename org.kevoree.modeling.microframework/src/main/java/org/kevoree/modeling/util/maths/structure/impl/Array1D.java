package org.kevoree.modeling.util.maths.structure.impl;

import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.util.maths.structure.KArray1D;
import org.kevoree.modeling.util.maths.structure.KArray2D;

public class Array1D implements KArray1D {

    private int _size;

    private int _offset;

    private int _segmentIndex;

    private KObjectChunk _segment;

    private KMetaClass _metaClass;

    public Array1D(int p_size, int p_offset, int p_segmentIndex, KObjectChunk p_segment, KMetaClass p_metaClass) {
        this._size = p_size;
        this._offset = p_offset;
        this._segmentIndex = p_segmentIndex;
        this._segment = p_segment;
        this._metaClass = p_metaClass;
    }

    @Override
    public int size() {
        return this._size;
    }

    @Override
    public double get(int p_index) {
        return this._segment.getDoubleArrayElem(this._segmentIndex, this._offset + p_index, this._metaClass);
    }

    @Override
    public double set(int p_index, double p_value) {
        this._segment.setDoubleArrayElem(this._segmentIndex, this._offset + p_index, p_value, this._metaClass);
        return p_value;
    }

    @Override
    public double add(int index, double value) {
        return set(index, get(index) + value);
    }

    @Override
    public double mult(int index, double value) {
        return set(index,get(index)*value);
    }

    @Override
    public void addAll(double value) {
        for(int i=0;i<_size;i++){
            add(i,value);
        }
    }

    @Override
    public void multAll(double value) {
        for(int i=0;i<_size;i++){
            mult(i,value);
        }
    }

    @Override
    public void setAll(double value) {
        //Fill value

    }

    @Override
    public void addElement(int index, int numElem) {

    }

    @Override
    public KArray2D clone() {
        return null;
    }


}
