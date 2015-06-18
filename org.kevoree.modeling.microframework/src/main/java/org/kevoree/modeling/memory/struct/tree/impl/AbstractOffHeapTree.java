package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 *
 * OffHeap implementation of AbstractOffHeapTree
 * - memory structure:  | root index (8) | size (4) | dirty (1) | counter (4) | back (size * node size * 8) |
 * - back:              | key (8)        | left (8) | right (8) | parent (8)  | color (8)   | value (8)     |
 */
public abstract class AbstractOffHeapTree implements KOffHeapMemoryElement {
    protected static final Unsafe UNSAFE = getUnsafe();

    public abstract int getNodeSize();

    private static final char BLACK_LEFT = '{';
    private static final char BLACK_RIGHT = '}';
    private static final char RED_LEFT = '[';
    private static final char RED_RIGHT = ']';

    private static final int POS_KEY = 0;
    private static final int POS_LEFT = 1;
    private static final int POS_RIGHT = 2;
    private static final int POS_PARENT = 3;
    private static final int POS_COLOR = 4;
    private static final int POS_VALUE = 5;

    protected long _start_address;
    protected int _threshold;
    protected float _loadFactor;

    private void internal_allocate(int size) {
        long bytes = internal_size_base_segment() + internal_size_raw_segment(size);

        _start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(_start_address, bytes, (byte) 0);

        UNSAFE.putLong(internal_ptr_root_index(), -1);
        UNSAFE.putInt(internal_ptr_size(), size);

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * _loadFactor);
    }

    protected int internal_size_base_segment() {
        return 8 + 4 + 1 + 4; // root index, size, dirty, counter
    }

    protected int internal_size_raw_segment(int size) {
        return size * 8 * getNodeSize();
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
        return internal_ptr_back() + idx * 8 * getNodeSize();
    }

    public int size() {
        return UNSAFE.getInt(internal_ptr_size());
    }

    protected long key(long p_nodeIndex) {
        if (p_nodeIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_KEY * 8);
    }

    protected void setKey(long p_nodeIndex, long p_key) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_KEY * 8, p_key);
    }

    protected long left(long p_nodeIndex) {
        if (p_nodeIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_LEFT * 8);
    }

    protected void setLeft(long p_nodeIndex, long p_leftNodeIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_LEFT * 8, p_leftNodeIndex);
    }

    protected long right(long p_nodeIndex) {
        if (p_nodeIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_RIGHT * 8);
    }

    protected void setRight(long p_nodeIndex, long p_rightNodeIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_RIGHT * 8, p_rightNodeIndex);
    }

    protected long parent(long p_nodeIndex) {
        if (p_nodeIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_PARENT * 8);
    }

    protected void setParent(long p_nodeIndex, long p_parentNodeIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_PARENT * 8, p_parentNodeIndex);
    }

    private long color(long p_nodeIndex) {
        if (p_nodeIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_COLOR * 8);
    }

    protected void setColor(long p_nodeIndex, long p_color) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_COLOR * 8, p_color);
    }

    protected long value(long p_nodeIndex) {
        if (p_nodeIndex == -1) {
            return -1;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_VALUE * 8);
    }

    protected void setValue(long p_nodeIndex, long p_value) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_VALUE * 8, p_value);
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

    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();
        long rootIndex = UNSAFE.getLong(internal_ptr_root_index());
        if (rootIndex == -1) {
            builder.append("0");
        } else {
            builder.append(size());
            builder.append(',');
            int elemSize = getNodeSize();
            builder.append(key(rootIndex));
            for (int i = 0; i < size(); i++) {
                long nextNodeIndex = i; /*i * elemSize;*/
                long parentNodeIndex = parent(nextNodeIndex);
                boolean isOnLeft = false;
                if (parentNodeIndex != -1) {
                    isOnLeft = left(parentNodeIndex) == nextNodeIndex;
                }
                if (color(nextNodeIndex) == 0) {
                    if (isOnLeft) {
                        builder.append(BLACK_LEFT);
                    } else {
                        builder.append(BLACK_RIGHT);
                    }
                } else {
                    //red
                    if (isOnLeft) {
                        builder.append(RED_LEFT);
                    } else {
                        builder.append(RED_RIGHT);
                    }
                }
                builder.append(key(nextNodeIndex));
                builder.append(',');
                if (parentNodeIndex != -1) {
                    //builder.append(beginParent / elemSize);
                    builder.append(key(parentNodeIndex));
                }
                if (elemSize > 5) {
                    builder.append(',');
                    builder.append(value(nextNodeIndex));
                }
            }
        }
        return builder.toString();
    }

    public void init(String payload, KMetaModel metaModel) {
        if (payload == null || payload.length() == 0) {
            internal_allocate(0);
            return;
        }
        int elemSize = getNodeSize();
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        int s = Integer.parseInt(payload.substring(initPos, cursor));
        internal_allocate(s);

        if (payload.charAt(cursor) == ',') {//className to parse
            UNSAFE.putInt(internal_ptr_size(), s);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        UNSAFE.putLong(internal_ptr_root_index(), Integer.parseInt(payload.substring(initPos, cursor)));
        UNSAFE.setMemory(internal_ptr_back(), internal_size_raw_segment(s), (byte) -1);

        int _back_index = 0;
        while (cursor < payload.length()) {
            while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                cursor++;
            }
            if (cursor < payload.length()) {
                char elem = payload.charAt(cursor);

                boolean isOnLeft = false;
                if (elem == BLACK_LEFT || elem == RED_LEFT) {
                    isOnLeft = true;
                }
                if (elem == BLACK_LEFT || elem == BLACK_RIGHT) {
                    setColor(_back_index, 0);
                } else {
                    setColor(_back_index, 1);
                }
                cursor++;
                int beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                    cursor++;
                }
                long loopKey = Long.parseLong(payload.substring(beginChunk, cursor));
                setKey(_back_index, loopKey);
                cursor++;
                beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    long parentRaw = Long.parseLong(payload.substring(beginChunk, cursor));
                    long parentValue = parentRaw; //* elemSize;
                    setParent(_back_index, parentValue);
                    if (isOnLeft) {
                        setLeft(parentValue, _back_index);
                    } else {
                        setRight(parentValue, _back_index);
                    }
                }
                if (cursor < payload.length() && payload.charAt(cursor) == ',') {
                    cursor++;
                    beginChunk = cursor;
                    while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                        cursor++;
                    }
                    if (cursor > beginChunk) {
                        long currentValue = Long.parseLong(payload.substring(beginChunk, cursor));
                        setValue(_back_index, currentValue);
                    }
                }
                _back_index++;
            }
        }
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
