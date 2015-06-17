package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;

public class OffHeapLongLongTree /*extends AbstractOffHeapTree implements KLongLongTree*/ {
/*
    public OffHeapLongLongTree() {
        _back = new long[_size * SIZE_NODE];
        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (_size * _loadFactor);
    }

    @Override
    public long previousOrEqualValue(long p_key) {
        long result = internal_previousOrEqual_index(p_key);
        if (result != -1) {
            return value(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long lookupValue(long p_key) {
        long n = _root_index;
        if (n == -1) {
            return KConfig.NULL_LONG;
        }
        while (n != -1) {
            if (p_key == key(n)) {
                return value(n);
            } else {
                if (p_key < key(n)) {
                    n = left(n);
                } else {
                    n = right(n);
                }
            }
        }
        return n;
    }

    @Override
    public synchronized void insert(long p_key, long p_value) {
        if ((_size + 1) > _threshold) {
            int length = (_size == 0 ? 1 : _size << 1);
            long[] new_back = new long[length * SIZE_NODE];
            System.arraycopy(_back, 0, new_back, 0, _size * SIZE_NODE);
            _threshold = (int) (_size * _loadFactor);
            _back = new_back;
        }
        long insertedNode = (_size) * SIZE_NODE;
        if (_size == 0) {
            _size = 1;
            setKey(insertedNode, p_key);
            setValue(insertedNode, p_value);
            setColor(insertedNode, 0);
            setLeft(insertedNode, -1);
            setRight(insertedNode, -1);
            setParent(insertedNode, -1);
            _root_index = insertedNode;
        } else {
            long n = _root_index;
            while (true) {
                if (p_key == key(n)) {
                    //nop _size
                    return;
                } else if (p_key < key(n)) {
                    if (left(n) == -1) {
                        setKey(insertedNode, p_key);
                        setValue(insertedNode, p_value);
                        setColor(insertedNode, 0);
                        setLeft(insertedNode, -1);
                        setRight(insertedNode, -1);
                        setParent(insertedNode, -1);
                        setLeft(n, insertedNode);
                        _size++;
                        break;
                    } else {
                        n = left(n);
                    }
                } else {
                    if (right(n) == -1) {
                        setKey(insertedNode, p_key);
                        setValue(insertedNode, p_value);
                        setColor(insertedNode, 0);
                        setLeft(insertedNode, -1);
                        setRight(insertedNode, -1);
                        setParent(insertedNode, -1);
                        setRight(n, insertedNode);
                        _size++;
                        break;
                    } else {
                        n = right(n);
                    }
                }
            }
            setParent(insertedNode, n);
        }
        insertCase1(insertedNode);
    }

    @Override
    public int getNodeSize() {
        return 6;
    }
*/
}
