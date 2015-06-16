package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KLongTree;

public class ArrayLongTree extends AbstractArrayTree implements KLongTree {

    private static final int SIZE_NODE = 5;

    public ArrayLongTree() {
        _back = new long[_size * SIZE_NODE];
        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (_size * _loadFactor);
    }

    public synchronized long previousOrEqual(long key) {
        long result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    public synchronized void insert(long key) {
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
            setKey(insertedNode, key);
            setColor(insertedNode, 0);
            setLeft(insertedNode, -1);
            setRight(insertedNode, -1);
            setParent(insertedNode, -1);
            _root_index = insertedNode;
        } else {
            long n = _root_index;
            while (true) {
                if (key == key(n)) {
                    //nop _size
                    return;
                } else if (key < key(n)) {
                    if (left(n) == -1) {
                        setKey(insertedNode, key);
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
                        setKey(insertedNode, key);
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

}
