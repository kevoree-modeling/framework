package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;

/** @ignore ts*/
public class OffHeapLongLongTree extends AbstractOffHeapTree implements KLongLongTree {

    @Override
    public int getNodeSize() {
        return 6;
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
        long n = UNSAFE.getLong(internal_ptr_root_index());
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

        if ((size() + 1) > _threshold) {
            int length = (size() == 0 ? 1 : size() << 1);

            int size_base_segment = internal_size_base_segment();
            int size_raw_segment = length * getNodeSize() * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);

            _threshold = (int) (size() * _loadFactor);
        }

        //long insertedNode = (size()) * getNodeSize();
        long insertedNode = p_key;//size() * SIZE_NODE;
        if (size() == 0) {
            UNSAFE.putInt(internal_ptr_size(), 1);

            setKey(insertedNode, p_key);
            setValue(insertedNode, p_value);
            setColor(insertedNode, 0);
            setLeft(insertedNode, -1);
            setRight(insertedNode, -1);
            setParent(insertedNode, -1);

            UNSAFE.putLong(internal_ptr_root_index(), insertedNode);
        } else {
            long n = UNSAFE.getLong(internal_ptr_root_index());
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

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
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

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
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
