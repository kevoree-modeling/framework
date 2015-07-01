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

            _threshold = (int) (length * _loadFactor);
        }

        long insertedNodeIndex = size();
        if (size() == 0) {
            UNSAFE.putInt(internal_ptr_size(), 1);

            setKey(insertedNodeIndex, p_key);
            setValue(insertedNodeIndex, p_value);
            setColor(insertedNodeIndex, 0);
            setLeft(insertedNodeIndex, -1);
            setRight(insertedNodeIndex, -1);
            setParent(insertedNodeIndex, -1);

            UNSAFE.putLong(internal_ptr_root_index(), insertedNodeIndex);
        } else {
            long rootIndex = UNSAFE.getLong(internal_ptr_root_index());
            while (true) {
                if (p_key == key(rootIndex)) {
                    //nop _size
                    return;
                } else if (p_key < key(rootIndex)) {
                    if (left(rootIndex) == -1) {
                        setKey(insertedNodeIndex, p_key);
                        setValue(insertedNodeIndex, p_value);
                        setColor(insertedNodeIndex, 0);
                        setLeft(insertedNodeIndex, -1);
                        setRight(insertedNodeIndex, -1);
                        setParent(insertedNodeIndex, -1);
                        setLeft(rootIndex, insertedNodeIndex);

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
                        break;
                    } else {
                        rootIndex = left(rootIndex);
                    }
                } else {
                    if (right(rootIndex) == -1) {
                        setKey(insertedNodeIndex, p_key);
                        setValue(insertedNodeIndex, p_value);
                        setColor(insertedNodeIndex, 0);
                        setLeft(insertedNodeIndex, -1);
                        setRight(insertedNodeIndex, -1);
                        setParent(insertedNodeIndex, -1);
                        setRight(rootIndex, insertedNodeIndex);

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
                        break;
                    } else {
                        rootIndex = right(rootIndex);
                    }
                }
            }
            setParent(insertedNodeIndex, rootIndex);
        }
        insertCase1(insertedNodeIndex);
    }


}
