package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class ArrayLongTree extends AbstractArrayTree implements KLongTree {

    public ArrayLongTree() {
        super();
        this._back_colors = null;
        this._back_meta = null;
        this._back_kv = null;
    }

    @Override
    int kvSize() {
        return 1;
    }

    public long previousOrEqual(long key) {
        int result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    public synchronized void insert(long key) {
        if ((_size + 1) > _threshold) {
            int length = (_size == 0 ? 1 : _size << 1);
            reallocate(length);
        }
        int newIndex = _size;
        if (newIndex == 0) {
            setKey(newIndex, key);
            setColor(newIndex, false);
            setLeft(newIndex, -1);
            setRight(newIndex, -1);
            setParent(newIndex, -1);
            _root_index = newIndex;
            _size = 1;
        } else {
            int n = _root_index;
            while (true) {
                if (key == key(n)) {
                    //nop _size
                    return;
                } else if (key < key(n)) {
                    if (left(n) == -1) {
                        setKey(newIndex, key);
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
                        setKey(newIndex, key);
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
