package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import sun.misc.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapLongTree extends AbstractOffHeapTree implements KLongTree, KOffHeapMemoryElement {

    private static final Unsafe UNSAFE = getUnsafe();

    public OffHeapLongTree() {
    }

    @Override
    public int getNodeSize() {
        return 5;
    }

    public synchronized void insert(long key) {

        if ((size() + 1) > _threshold) {
            int length = (size() == 0 ? 1 : size() << 1);

            int size_base_segment = internal_size_base_segment();
            int size_raw_segment = length * getNodeSize() * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);

            _threshold = (int) (size() * _loadFactor);
        }

        long insertedNode = key;//size() * SIZE_NODE;
        if (size() == 0) {
            UNSAFE.putInt(internal_ptr_size(), 1);

            setKey(insertedNode, key);
            setColor(insertedNode, 0);
            setLeft(insertedNode, -1);
            setRight(insertedNode, -1);
            setParent(insertedNode, -1);

            UNSAFE.putLong(internal_ptr_root_index(), insertedNode);
        } else {
            long n = UNSAFE.getLong(internal_ptr_root_index());
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

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
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

    @Override
    public long previousOrEqual(long key) {
        long result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        _start_address = address;

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * _loadFactor);
    }
}
