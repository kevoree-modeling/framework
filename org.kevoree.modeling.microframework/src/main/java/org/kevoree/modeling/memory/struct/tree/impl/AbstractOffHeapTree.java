package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/** @ignore ts
 * OffHeap implementation of AbstractOffHeapTree
 * - memory structure:  | root index (8) | size (4) | dirty (1) | counter (4) | back (size * 8) |
 */
public abstract class AbstractOffHeapTree implements KOffHeapMemoryElement {
    protected static final Unsafe UNSAFE = getUnsafe();

    private final int SIZE_NODE = getNodeSize();

    public abstract int getNodeSize();

    private static final int BLACK = 0;
    private static final int RED = 1;

    protected long _start_address;
    protected int _threshold;
    protected float _loadFactor;

    private int internal_size_base_segment() {
        return 8 + 4 + 1 + 4; // root index, size, dirty, counter
    }

    protected long internal_ptr_root_index() {
        return _start_address;
    }

    protected long internal_ptr_size() {
        return internal_ptr_root_index() + 8;
    }

    protected long internal_ptr_dirty() {
        return internal_ptr_size() + 4;
    }

    protected long internal_ptr_counter() {
        return internal_ptr_dirty() + 1;
    }

    protected long internal_ptr_back() {
        return internal_ptr_counter() + 4;
    }

    protected long internal_ptr_back_idx(long idx) {
        return internal_ptr_back() + idx * 8 * SIZE_NODE;
    }

    public int size() {
        return UNSAFE.getInt(internal_ptr_size());
    }

    protected long left(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_currentIndex));
    }

    protected void setLeft(long p_currentIndex, long p_paramIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_currentIndex), p_paramIndex);

    }

    protected long right(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_currentIndex) + 1 * 8);
    }

    protected void setRight(long p_currentIndex, long p_paramIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_currentIndex) + 1 * 8, p_paramIndex);
    }

    private long parent(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_currentIndex) + 2 * 8);
    }

    protected void setParent(long p_currentIndex, long p_paramIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_currentIndex) + 2 * 8, p_paramIndex);
    }

    protected long key(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_currentIndex) + 3 * 8);
    }

    protected void setKey(long p_currentIndex, long p_paramIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_currentIndex) + 3 * 8, p_paramIndex);
    }

    private long color(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(currentIndex) + 4 * 8);
    }

    protected void setColor(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(currentIndex) + 4 * 8, paramIndex);
    }

    protected long value(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(currentIndex) + 5 * 8);
    }

    protected void setValue(long currentIndex, long paramIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(currentIndex) + 5 * 8, paramIndex);
    }

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

    private long previous(long p_index) {
        long p = p_index;
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

    /* Time never use direct lookup, sadly for performance, anyway this method is private to ensure the correctness of caching mechanism */
    public long lookup(long p_key) {
        long n = UNSAFE.getLong(internal_ptr_root_index());
        if (n == -1) {
            return KConfig.NULL_LONG;
        }
        while (n != -1) {
            if (p_key == key(n)) {
                return key(n);
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

    public void range(long startKey, long endKey, KTreeWalker walker) {
        long indexEnd = internal_previousOrEqual_index(endKey);
        while (indexEnd != -1 && key(indexEnd) >= startKey) {
            walker.elem(key(indexEnd));
            indexEnd = previous(indexEnd);
        }
    }

    protected long internal_previousOrEqual_index(long p_key) {
        long p = UNSAFE.getLong(internal_ptr_root_index());
        if (p == -1) {
            return p;
        }
        while (p != -1) {
            if (p_key == key(p)) {
                return p;
            }
            if (p_key > key(p)) {
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
            UNSAFE.putLong(internal_ptr_root_index(), newn);
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

    protected void insertCase1(long n) {
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

    /*
    public void delete(long key) {
        TreeNode n = lookup(key);
        if (n == null) {
            return;
        } else {
            _size--;
            if (n.getLeft() != null && n.getRight() != null) {
                // Copy domainKey/value from predecessor and done delete it instead
                TreeNode pred = n.getLeft();
                while (pred.getRight() != null) {
                    pred = pred.getRight();
                }
                n.key = pred.key;
                n = pred;
            }
            TreeNode child;
            if (n.getRight() == null) {
                child = n.getLeft();
            } else {
                child = n.getRight();
            }
            if (nodeColor(n) == true) {
                n.color = nodeColor(child);
                deleteCase1(n);
            }
            replaceNode(n, child);
        }
    }

    private void deleteCase1(TreeNode n) {
        if (n.getParent() == null) {
            return;
        } else {
            deleteCase2(n);
        }
    }

    private void deleteCase2(TreeNode n) {
        if (nodeColor(n.sibling()) == false) {
            n.getParent().color = false;
            n.sibling().color = true;
            if (n == n.getParent().getLeft()) {
                rotateLeft(n.getParent());
            } else {
                rotateRight(n.getParent());
            }
        }
        deleteCase3(n);
    }

    private void deleteCase3(TreeNode n) {
        if (nodeColor(n.getParent()) == true && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == true && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            deleteCase1(n.getParent());
        } else {
            deleteCase4(n);
        }
    }

    private void deleteCase4(TreeNode n) {
        if (nodeColor(n.getParent()) == false && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == true && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            n.getParent().color = true;
        } else {
            deleteCase5(n);
        }
    }

    private void deleteCase5(TreeNode n) {
        if (n == n.getParent().getLeft() && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == false && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            n.sibling().getLeft().color = true;
            rotateRight(n.sibling());
        } else if (n == n.getParent().getRight() && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getRight()) == false && nodeColor(n.sibling().getLeft()) == true) {
            n.sibling().color = false;
            n.sibling().getRight().color = true;
            rotateLeft(n.sibling());
        }
        deleteCase6(n);
    }

    private void deleteCase6(TreeNode n) {
        n.sibling().color = nodeColor(n.getParent());
        n.getParent().color = true;
        if (n == n.getParent().getLeft()) {
            n.sibling().getRight().color = true;
            rotateLeft(n.getParent());
        } else {
            n.sibling().getLeft().color = true;
            rotateRight(n.getParent());
        }
    }*/

    public void delete(long p_key) {
        //TODO
    }

    private boolean nodeColor(long n) {
        if (n == -1) {
            return true;
        } else {
            return color(n) == 1;
        }
    }

    private void node_serialize(StringBuilder builder, long current) {
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

    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();
        builder.append(size());

        long _root_index = UNSAFE.getLong(internal_ptr_root_index());
        if (_root_index != -1) {
            node_serialize(builder, _root_index);
        }
        return builder.toString();
    }

    public void init(String payload, KMetaModel metaModel) {
        _start_address = UNSAFE.allocateMemory(internal_size_base_segment()); // allocate memory for base segment
        UNSAFE.setMemory(_start_address, internal_size_base_segment(), (byte) 0);
        UNSAFE.putLong(internal_ptr_root_index(), -1);

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * _loadFactor);

    }

    public boolean isDirty() {
        return UNSAFE.getByte(internal_ptr_dirty()) != 0;
    }

    public void setClean(KMetaModel p_metaModel) {
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 0);
    }

    public void setDirty() {
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 1);
    }

    public int counter() {
        return UNSAFE.getInt(internal_ptr_counter());
    }

    public void inc() {
        int c = UNSAFE.getInt(internal_ptr_counter());
        UNSAFE.putInt(internal_ptr_counter(), c + 1);
    }

    public void dec() {
        int c = UNSAFE.getInt(internal_ptr_counter());
        UNSAFE.putInt(internal_ptr_counter(), c - 1);
    }

    public void free(KMetaModel p_metaModel) {
        UNSAFE.freeMemory(_start_address);
    }


    @SuppressWarnings("restriction")
    protected static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
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
