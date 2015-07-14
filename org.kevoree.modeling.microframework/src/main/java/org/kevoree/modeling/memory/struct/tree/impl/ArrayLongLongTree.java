package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;

public class ArrayLongLongTree extends AbstractArrayTree implements KLongLongTree {

    public ArrayLongLongTree() {
        super();
        this._back_colors = null;
        this._back_meta = null;
        this._back_kv = null;
        this.kvSize = 2;
    }

    @Override
    public long previousOrEqualValue(long p_key) {
        int result = internal_previousOrEqual_index(p_key);
        if (result != -1) {
            return value(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long lookupValue(long p_key) {
        int n = _root_index;
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
            reallocate(length);
        }
        int newIndex = _size;
        if (newIndex == 0) {
            setKey(newIndex, p_key);
            setValue(newIndex, p_value);
            setColor(newIndex, false);
            setLeft(newIndex, -1);
            setRight(newIndex, -1);
            setParent(newIndex, -1);
            _root_index = newIndex;
            _size = 1;
        } else {
            int n = _root_index;
            while (true) {
                if (p_key == key(n)) {
                    //nop _size
                    return;
                } else if (p_key < key(n)) {
                    if (left(n) == -1) {
                        setKey(newIndex, p_key);
                        setValue(newIndex, p_value);
                        setColor(newIndex, false);
                        setLeft(newIndex, -1);
                        setRight(newIndex, -1);
                        setParent(newIndex, -1);
                        setLeft(n, newIndex);
                        _size++;
                        break;
                    } else {
                        n = left(n);
                    }
                } else {
                    if (right(n) == -1) {
                        setKey(newIndex, p_key);
                        setValue(newIndex, p_value);
                        setColor(newIndex, false);
                        setLeft(newIndex, -1);
                        setRight(newIndex, -1);
                        setParent(newIndex, -1);
                        setRight(n, newIndex);
                        _size++;
                        break;
                    } else {
                        n = right(n);
                    }
                }
            }
            setParent(newIndex, n);
        }
        insertCase1(newIndex);
    }

}
