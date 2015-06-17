package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapLongTree extends AbstractOffHeapTree implements KLongTree, KOffHeapMemoryElement {
    @Override
    public int getNodeSize() {
        return 0;
    }

    @Override
    public void insert(long key) {

    }

    @Override
    public long previousOrEqual(long key) {
        return 0;
    }
//    private static final Unsafe UNSAFE = getUnsafe();
//
//    public OffHeapLongTree() {
//    }
//
//    @Override
//    public int getNodeSize() {
//        return 5;
//    }
//
//
//    public synchronized void insert(long key) {
//
//        if ((size() + 1) > _threshold) {
//            int length = (size() == 0 ? 1 : size() << 1);
//
//            int size_base_segment = internal_size_of_base_segment();
//            int size_raw_segment = length * SIZE_NODE * 8;
//            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);
//
//            _threshold = (int) (size() * loadFactor);
//        }
//
//        long insertedNode = key;//size() * SIZE_NODE;
//        if (size() == 0) {
//            internal_set_size(1);
//
//            setKey(insertedNode, key);
//            setColor(insertedNode, 0);
//            setLeft(insertedNode, -1);
//            setRight(insertedNode, -1);
//            setParent(insertedNode, -1);
//
//            internal_set_root_index(insertedNode);
//        } else {
//            long n = internal_get_root_index();
//            while (true) {
//                if (key == key(n)) {
//                    //nop _size
//                    return;
//                } else if (key < key(n)) {
//                    if (left(n) == -1) {
//
//                        setKey(insertedNode, key);
//                        setColor(insertedNode, 0);
//                        setLeft(insertedNode, -1);
//                        setRight(insertedNode, -1);
//                        setParent(insertedNode, -1);
//
//                        setLeft(n, insertedNode);
//
//                        //UNSAFE.putInt(internal_ptr_size(), size() + 1); // size++
//                        internal_set_size(size() + 1);
//                        break;
//                    } else {
//                        n = left(n);
//                    }
//                } else {
//                    if (right(n) == -1) {
//
//                        setKey(insertedNode, key);
//                        setColor(insertedNode, 0);
//                        setLeft(insertedNode, -1);
//                        setRight(insertedNode, -1);
//                        setParent(insertedNode, -1);
//
//                        setRight(n, insertedNode);
//
//                        internal_set_size(size() + 1);
//                        break;
//                    } else {
//                        n = right(n);
//                    }
//                }
//            }
//
//            setParent(insertedNode, n);
//        }
//        insertCase1(insertedNode);
//    }
//
//    @Override
//    public long previousOrEqual(long key) {
//        long result = previousOrEqualIndex(key);
//        if (result != -1) {
//            return key(result);
//        } else {
//            return KConfig.NULL_LONG;
//        }
//    }
//
//
//    private long previousOrEqualIndex(long key) {
//        long p = internal_get_root_index();
//        if (p == -1) {
//            return p;
//        }
//        while (p != -1) {
//            if (key == key(p)) {
//                return p;
//            }
//            if (key > key(p)) {
//                if (right(p) != -1) {
//                    p = right(p);
//                } else {
//                    return p;
//                }
//            } else {
//                if (left(p) != -1) {
//                    p = left(p);
//                } else {
//                    long parent = parent(p);
//                    long ch = p;
//                    while (parent != -1 && ch == left(parent)) {
//                        ch = parent;
//                        parent = parent(parent);
//                    }
//                    return parent;
//                }
//            }
//        }
//        return -1;
//    }
//
//    private boolean nodeColor(long n) {
//        if (n == -1) {
//            return true;
//        } else {
//            return color(n) == 1;
//        }
//    }
//
//    @Override
//    public String serialize(KMetaModel metaModel) {
//        StringBuilder builder = new StringBuilder();
//        builder.append(size());
//        if (internal_get_root_index() != -1) {
//            node_serialize(builder, internal_get_root_index());
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public long getMemoryAddress() {
//        return _start_address;
//    }
//
//    @Override
//    public void setMemoryAddress(long address) {
//        _start_address = address;
//
//        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
//        _threshold = (int) (size() * _loadFactor);
//    }
}
