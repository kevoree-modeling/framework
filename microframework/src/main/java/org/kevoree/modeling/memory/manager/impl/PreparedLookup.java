package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.KPreparedLookup;

public class PreparedLookup implements KPreparedLookup {

    private final int _size;

    private final long[] _flatStatement;

    private int _current = -1;

    public PreparedLookup(int p_size) {
        _size = p_size;
        _flatStatement = new long[p_size * 3];
        _current = 0;
    }

    @Override
    public void addLookupOperation(long universe, long time, long uuid) {
        if (_current >= _size) {
            throw new RuntimeException("PreparedLookup is full, prepared capacity:" + _size);
        }
        _flatStatement[_current * 3] = universe;
        _flatStatement[_current * 3 + 1] = time;
        _flatStatement[_current * 3 + 2] = uuid;
        _current++;
    }

    @Override
    public long[] flatLookup() {
        return _flatStatement;
    }
}
