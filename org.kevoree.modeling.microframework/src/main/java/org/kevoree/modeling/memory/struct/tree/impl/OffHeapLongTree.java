package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * OffHeap implementation of KLongTree
 * - Memory structure: |size      |dirty    |counter  |root index   |tree structure |
 * -                   |(4 byte)  |(1 byte) |(4 byte) |(8 byte)     |(size * 8 byte)|
 */
public class OffHeapLongTree implements KLongTree, KOffHeapMemoryElement {
    private static final Unsafe UNSAFE = getUnsafe();

    private static final int SIZE_NODE = 5;

    private static final int BLACK = 0; // color == true (yep, this is a strange notation)
    private static final int RED = 1; // color == false (yep, this is a strange notation)

    private int _threshold;
    private float loadFactor;

    private long _start_address;

    public OffHeapLongTree() {
    }

    @Override
    public void init(String payload, KMetaModel metaModel) throws Exception {
        _start_address = UNSAFE.allocateMemory(internal_size_of_base_segment()); // allocate memory for base segment
        UNSAFE.setMemory(_start_address, internal_size_of_base_segment(), (byte) 0);
        internal_set_root_index(-1);

        loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * loadFactor);
    }

    private long internal_ptr_size() {
        return _start_address;
    }

    private long internal_ptr_dirty() {
        return _start_address + 4;
    }

    private long internal_ptr_counter() {
        return internal_ptr_dirty() + 1;
    }

    private long internal_ptr_root_index() {
        return internal_ptr_counter() + 4;
    }

    private long internal_ptr_for(long index) {
        return internal_ptr_root_index() + 8 + index * 8 * SIZE_NODE;
    }

    private void internal_set_size(int newSize) {
        UNSAFE.putInt(internal_ptr_size(), newSize);
    }

    private void internal_set_root_index(long newIndex) {
        UNSAFE.putLong(internal_ptr_root_index(), newIndex);
    }

    private long internal_get_root_index() {
        return UNSAFE.getLong(internal_ptr_root_index());
    }

    private int internal_size_of_base_segment() {
        return 4 + 1 + 4 + 8; // size + dirty + counter + root index
    }

    private long left(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_for(currentIndex));
    }

    private void setLeft(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_for(currentIndex), paramIndex);
    }

    private long right(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_for(currentIndex) + 1 * 8);
    }

    private void setRight(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_for(currentIndex) + 1 * 8, paramIndex);
    }

    private long parent(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_for(currentIndex) + 2 * 8);
    }

    private void setParent(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_for(currentIndex) + 2 * 8, paramIndex);
    }

    private long key(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_for(currentIndex) + 3 * 8);
    }

    private void setKey(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_for(currentIndex) + 3 * 8, paramIndex);
    }

    private long color(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_for(currentIndex) + 4 * 8);
    }

    private void setColor(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_for(currentIndex) + 4 * 8, paramIndex);
    }

    /* offheap end */

    public long grandParent(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        if (parent(currentIndex) != -1) {
            return parent(parent(currentIndex));
        } else {
            return -1;
        }
    }

    public long sibling(long currentIndex) {
        if (parent(currentIndex) == -1) {
            return -1;
        } else {
            if (currentIndex == left(parent(currentIndex))) {
                return right(parent(currentIndex));
            } else {
                return left(parent(currentIndex));
            }
        }
    }

    public long uncle(long currentIndex) {
        if (parent(currentIndex) != -1) {
            return sibling(parent(currentIndex));
        } else {
            return -1;
        }
    }


    /* Time never use direct lookup, sadly for performance, anyway this method is private to ensure the correctness of caching mechanism */
    public long lookup(long key) {
        long n = internal_get_root_index();
        if (n == -1) {
            return KConfig.NULL_LONG;
        }
        while (n != -1) {
            if (key == key(n)) {
                return key(n);
            } else {
                if (key < key(n)) {
                    n = left(n);
                } else {
                    n = right(n);
                }
            }
        }
        return n;
    }

    @Override
    public void range(long startKey, long endKey, KTreeWalker walker) {
        long indexEnd = previousOrEqualIndex(endKey);
        while (indexEnd != -1 && key(indexEnd) >= startKey) {
            walker.elem(key(indexEnd));
            indexEnd = previous(indexEnd);
        }
    }


    @Override
    public void delete(long key) {
        throw new RuntimeException("Not yet implemented!");

//        long n = lookup(key);
//        if (n == KConfig.NULL_LONG) {
//            return;
//
//        } else {
//            internal_set_size(size() - 1);
//            if (left(n) != -1 && right(n) != -1) {
//                // Copy domainKey/value from predecessor and done delete it instead
//                long pred = left(n);
//                while (right(pred) != -1) {
//                    pred = right(pred);
//                }
//                n = pred;
//            }
//            long child;
//            if (right(n) == -1) {
//                child = left(n);
//            } else {
//                child = right(n);
//            }
//            if (nodeColor(n) == true) {
//                if (nodeColor(child) == true) {
//                    setColor(n, BLACK);
//                } else {
//                    setColor(n, RED);
//                }
//                deleteCase1(n);
//            }
//            replaceNode(n, child);
//
//            // ?
//            setLeft(key, -1);
//            setRight(key, -1);
//            setParent(key, -1);
//            setKey(key, -1);
//            setColor(key, -1);
//        }
    }

    private void deleteCase1(long n) {
        if (parent(n) == -1) {
            return;
        } else {
            deleteCase2(n);
        }
    }

    private void deleteCase2(long n) {
        if (nodeColor(sibling(n)) == false) {
            setColor(parent(n), RED);
            setColor(sibling(n), BLACK);
            if (n == left(parent(n))) {
                rotateLeft(parent(n));
            } else {
                rotateRight(parent(n));
            }
        }
        deleteCase3(n);
    }

    private void deleteCase3(long n) {
        if (nodeColor(parent(n)) == true && nodeColor(sibling(n)) == true && nodeColor(left(sibling(n))) == true && nodeColor(right(sibling(n))) == true) {
            setColor(sibling(n), RED);
            deleteCase1(parent(n));
        } else {
            deleteCase4(n);
        }
    }

    private void deleteCase4(long n) {
        if (nodeColor(parent(n)) == false && nodeColor(sibling(n)) == true && nodeColor(left(sibling(n))) == true && nodeColor(right(sibling(n))) == true) {
            setColor(sibling(n), RED);
            setColor(parent(n), BLACK);
        } else {
            deleteCase5(n);
        }
    }

    private void deleteCase5(long n) {

        if (n == left(parent(n)) && nodeColor(sibling(n)) == true && nodeColor(left(sibling(n))) == false && nodeColor(right(sibling(n))) == true) {
            setColor(sibling(n), RED);
            setColor(left(sibling(n)), BLACK);
            rotateRight(sibling(n));

        } else if (n == right(parent(n)) && nodeColor(sibling(n)) == true && nodeColor(right(sibling(n))) == false && nodeColor(left(sibling(n))) == true) {
            setColor(sibling(n), RED);
            setColor(right(sibling(n)), BLACK);
            rotateLeft(sibling(n));
        }
        deleteCase6(n);
    }

    private void deleteCase6(long n) {
        if (nodeColor(parent(n)) == true) {
            setColor(sibling(n), BLACK);
        } else {
            setColor(sibling(n), RED);
        }
        setColor(parent(n), BLACK);

        if (n == left(parent(n))) {
            setColor(right(sibling(n)), BLACK);
            rotateLeft(parent(n));
        } else {
            setColor(left(sibling(n)), BLACK);
            rotateRight(parent(n));
        }
    }

    private void rotateLeft(long n) {
        long r = right(n);
        replaceNode(n, r);
        setRight(n, left(r));
        if (left(r) != -1) {
            setParent(left(r), n);
        }
        setLeft(r, n);
        setParent(n, r);
    }

    private void rotateRight(long n) {
        long l = left(n);
        replaceNode(n, l);
        setLeft(n, right(l));
        if (right(l) != -1) {
            setParent(right(l), n);
        }
        setRight(l, n);
        setParent(n, l);
    }

    private void replaceNode(long oldn, long newn) {
        if (parent(oldn) == -1) {
            internal_set_root_index(newn);
        } else {
            if (oldn == left(parent(oldn))) {
                setLeft(parent(oldn), newn);
            } else {
                setRight(parent(oldn), newn);
            }
        }
        if (newn != -1) {
            setParent(newn, parent(oldn));
        }
    }

    public synchronized void insert(long key) {

        if ((size() + 1) > _threshold) {
            int length = (size() == 0 ? 1 : size() << 1);

            int size_base_segment = internal_size_of_base_segment();
            int size_raw_segment = length * SIZE_NODE * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);

            _threshold = (int) (size() * loadFactor);
        }

        long insertedNode = key;//size() * SIZE_NODE;
        if (size() == 0) {
            internal_set_size(1);

            setKey(insertedNode, key);
            setColor(insertedNode, 0);
            setLeft(insertedNode, -1);
            setRight(insertedNode, -1);
            setParent(insertedNode, -1);

            internal_set_root_index(insertedNode);
        } else {
            long n = internal_get_root_index();
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

                        //UNSAFE.putInt(internal_ptr_size(), size() + 1); // size++
                        internal_set_size(size() + 1);
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

                        internal_set_size(size() + 1);
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
        long result = previousOrEqualIndex(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    private void insertCase1(long n) {
        if (parent(n) == -1) {
            setColor(n, 1);
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(long n) {
        if (nodeColor(parent(n)) == true) {
            return;
        } else {
            insertCase3(n);
        }
    }

    private void insertCase3(long n) {
        if (nodeColor(uncle(n)) == false) {
            setColor(parent(n), 1);
            setColor(uncle(n), 1);
            setColor(grandParent(n), 0);
            insertCase1(grandParent(n));
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(long n_n) {
        long n = n_n;
        if (n == right(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateLeft(parent(n));
            n = left(n);
        } else {
            if (n == left(parent(n)) && parent(n) == right(grandParent(n))) {
                rotateRight(parent(n));
                n = right(n);
            }
        }
        insertCase5(n);
    }

    private void insertCase5(long n) {
        setColor(parent(n), 1);
        setColor(grandParent(n), 0);
        if (n == left(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateRight(grandParent(n));
        } else {
            rotateLeft(grandParent(n));
        }
    }


    private long previous(long index) {
        long p = index;
        if (left(p) != -1) {
            p = left(p);
            while (right(p) != -1) {
                p = right(p);
            }
            return p;
        } else {
            if (parent(p) != -1) {
                if (p == right(parent(p))) {
                    return parent(p);
                } else {
                    while (parent(p) != -1 && p == left(parent(p))) {
                        p = parent(p);
                    }
                    return parent(p);
                }
            } else {
                return -1;
            }
        }
    }

    private long previousOrEqualIndex(long key) {
        long p = internal_get_root_index();
        if (p == -1) {
            return p;
        }
        while (p != -1) {
            if (key == key(p)) {
                return p;
            }
            if (key > key(p)) {
                if (right(p) != -1) {
                    p = right(p);
                } else {
                    return p;
                }
            } else {
                if (left(p) != -1) {
                    p = left(p);
                } else {
                    long parent = parent(p);
                    long ch = p;
                    while (parent != -1 && ch == left(parent)) {
                        ch = parent;
                        parent = parent(parent);
                    }
                    return parent;
                }
            }
        }
        return -1;
    }

    private boolean nodeColor(long n) {
        if (n == -1) {
            return true;
        } else {
            return color(n) == 1;
        }
    }

    public void node_serialize(StringBuilder builder, long current) {
        builder.append("|");
        if (nodeColor(current) == true) {
            builder.append(BLACK);
        } else {
            builder.append(RED);
        }
        builder.append(key(current));
        if (left(current) == -1 && right(current) == -1) {
            builder.append("%");
        } else {
            if (left(current) != -1) {
                node_serialize(builder, left(current));
            } else {
                builder.append("#");
            }
            if (right(current) != -1) {
                node_serialize(builder, right(current));
            } else {
                builder.append("#");
            }
        }
    }


    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }

    @Override
    public boolean isDirty() {
        return UNSAFE.getByte(internal_ptr_dirty()) != 0;
    }

    @Override
    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();
        builder.append(size());
        if (internal_get_root_index() != -1) {
            node_serialize(builder, internal_get_root_index());
        }
        return builder.toString();
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 0);
    }

    @Override
    public void setDirty() {
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 1);
    }

    @Override
    public int counter() {
        return UNSAFE.getInt(internal_ptr_counter());
    }

    @Override
    public void inc() {
        int c = UNSAFE.getInt(internal_ptr_counter());
        UNSAFE.putInt(internal_ptr_counter(), c + 1);
    }

    @Override
    public void dec() {
        int c = UNSAFE.getInt(internal_ptr_counter());
        UNSAFE.putInt(internal_ptr_counter(), c - 1);
    }

    @Override
    public void free(KMetaModel metaModel) {
        UNSAFE.freeMemory(_start_address);
    }

    @Override
    public int size() {
        return UNSAFE.getInt(internal_ptr_size());
    }

    private void debugSegments() {
        for (int i = 0; i < size(); i++) {
            System.out.println("size " + i + " | "
                            + "left " + left(i) + " | "
                            + "right " + right(i) + " | "
                            + "parent " + parent(i) + " | "
                            + "key " + key(i) + " | "
                            + "color " + color(i) + " | "
            );
        }
    }

    @Override
    public long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        _start_address = address;

        loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * loadFactor);
    }
}
