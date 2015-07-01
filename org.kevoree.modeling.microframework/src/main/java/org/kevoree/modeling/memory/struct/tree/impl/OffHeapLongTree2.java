package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import sun.misc.Unsafe;

/**
 * @ignore ts
 */
public class
        OffHeapLongTree2 extends AbstractOffHeapTree2 implements KLongTree, KOffHeapMemoryElement {

    private static final Unsafe UNSAFE = getUnsafe();

    public OffHeapLongTree2() {
    }

    @Override
    public int getNodeSize() {
        return 5;
    }

    public synchronized void insert(long key) {
        int size = UNSAFE.getInt(_start_address + 8);
        if ((size + 1) > _threshold) {
            int length = (size == 0 ? 1 : size << 1);

            int size_base_segment = 17;
            int size_raw_segment = length * getNodeSize() * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);

            _threshold = (int) (length * _loadFactor);
        }

        long insertedNodeIndex = size;

        if (size == 0) {
            UNSAFE.putInt(_start_address + 8, 1);

            // key
            UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize(), 0);
            // color
            UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 32, 0);
            // left
            UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 8, -1);
            // right
            UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 16, -1);
            // parent
            UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 24, -1);

            // root index
            UNSAFE.putLong(_start_address, insertedNodeIndex);
        } else {
            long rootIndex = UNSAFE.getLong(_start_address);
            long k = rootIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + rootIndex * 8 * getNodeSize());
            while (true) {
                if (key == k) {
                    //nop _size
                    return;
                } else if (key < k) {
                    long l = rootIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + rootIndex * 8 * getNodeSize() + 8);
                    if (l == -1) {

                        // key
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize(), key);
                        // color
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 32, 0);
                        // left
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 8, -1);
                        // right
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 16, -1);
                        // parent
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 24, -1);
                        // left
                        UNSAFE.putLong(_start_address + 17 + rootIndex * 8 * getNodeSize() + 8, insertedNodeIndex);

                        // size
                        UNSAFE.putInt(_start_address + 8, UNSAFE.getInt(_start_address + 8) + 1);
                        break;
                    } else {
                        rootIndex = l;
                    }
                } else {
                    long r = rootIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + rootIndex * 8 * getNodeSize() + 16);
                    if (r == -1) {

                        // key
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize(), key);
                        // color
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 32, 0);
                        // left
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 8, -1);
                        // right
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 16, -1);
                        // parent
                        UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 24, -1);
                        // right
                        UNSAFE.putLong(_start_address + 17 + rootIndex * 8 * getNodeSize() + 16, insertedNodeIndex);

                        // size
                        UNSAFE.putInt(_start_address + 8, UNSAFE.getInt(_start_address + 8) + 1);
                        break;
                    } else {
                        rootIndex = r;
                    }
                }
            }

            //parent
            UNSAFE.putLong(_start_address + 17 + insertedNodeIndex * 8 * getNodeSize() + 24, rootIndex);
        }
        insertCase1(insertedNodeIndex);
    }

    @Override
    public long previousOrEqual(long key) {
        long result = internal_previousOrEqual_index(key);
        if (result != -1) {
            // key
            return UNSAFE.getLong(_start_address + 17 + result * 8 * getNodeSize());
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
        _threshold = (int) (UNSAFE.getInt(_start_address + 8) * _loadFactor);
    }
}
