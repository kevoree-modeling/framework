package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 * <p/>
 * OffHeap implementation of AbstractOffHeapTree
 * - memory structure:  | root index (8) | size (4) | dirty (1) | counter (4) | back (size * node size * 8) |
 * - back:              | key (8)        | left (8) | right (8) | parent (8)  | color (8)   | value (8)     |
 */
public abstract class AbstractOffHeapTree3 implements KOffHeapMemoryElement {
    protected static final Unsafe UNSAFE = getUnsafe();

    protected static int NODE_SIZE;

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

    private static final int ZERO = 0;
    private static final int UNDEFINED = -1;
    private static final int BYTE = 8;

    private static final int ATT_ROOT_INDEX_LEN = 8;
    private static final int ATT_SIZE_LEN = 4;
    private static final int ATT_DIRTY_LEN = 1;
    private static final int ATT_COUNTER_LEN = 4;

    private static final int OFFSET_ROOT_INDEX = 0;
    private static final int OFFSET_SIZE = OFFSET_ROOT_INDEX + ATT_ROOT_INDEX_LEN;
    private static final int OFFSET_DIRTY = OFFSET_SIZE + ATT_SIZE_LEN;
    private static final int OFFSET_COUNTER = OFFSET_DIRTY + ATT_DIRTY_LEN;
    private static final int OFFSET_BACK = OFFSET_COUNTER + ATT_COUNTER_LEN;


    private static final int BASE_SEGMENT_LEN = ATT_ROOT_INDEX_LEN + ATT_SIZE_LEN + ATT_DIRTY_LEN + ATT_COUNTER_LEN;

    protected static long _start_address;
    protected int _threshold;
    protected float _loadFactor;

    protected AbstractOffHeapTree3() {
        NODE_SIZE = 0;
    }

    private void internal_allocate(int size) {
        long bytes = BASE_SEGMENT_LEN + internal_size_raw_segment(size);

        _start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(_start_address, bytes, (byte) ZERO);

        UNSAFE.putLong(_start_address + OFFSET_ROOT_INDEX, UNDEFINED);
        UNSAFE.putInt(_start_address + OFFSET_SIZE, size);

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * _loadFactor);
    }

    private static final int internal_size_raw_segment(int size) {
        return size * BYTE * NODE_SIZE;
    }

    private static final long internal_ptr_back_idx(long idx) {
        return _start_address + OFFSET_BACK + idx * BYTE * NODE_SIZE;
    }

    public int size() {
        return UNSAFE.getInt(_start_address + OFFSET_SIZE);
    }

    protected static final long key(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_KEY * BYTE);
    }

    private static final void setKey(long p_nodeIndex, long p_key) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_KEY * BYTE, p_key);
    }

    private static final long left(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_LEFT * BYTE);
    }

    private static final void setLeft(long p_nodeIndex, long p_leftNodeIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_LEFT * BYTE, p_leftNodeIndex);
    }

    private static final long right(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_RIGHT * BYTE);
    }

    private static final void setRight(long p_nodeIndex, long p_rightNodeIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_RIGHT * BYTE, p_rightNodeIndex);
    }

    private static final long parent(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_PARENT * BYTE);
    }

    private static final void setParent(long p_nodeIndex, long p_parentNodeIndex) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_PARENT * BYTE, p_parentNodeIndex);
    }

    private static final long color(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_COLOR * BYTE);
    }

    private static final void setColor(long p_nodeIndex, long p_color) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_COLOR * BYTE, p_color);
    }

    protected static final long value(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        return UNSAFE.getLong(internal_ptr_back_idx(p_nodeIndex) + POS_VALUE * BYTE);
    }

    private static final void setValue(long p_nodeIndex, long p_value) {
        UNSAFE.putLong(internal_ptr_back_idx(p_nodeIndex) + POS_VALUE * BYTE, p_value);
    }

    private static final long grandParent(long currentIndex) {
        if (currentIndex == UNDEFINED) {
            return UNDEFINED;
        }
        if (parent(currentIndex) != UNDEFINED) {
            return parent(parent(currentIndex));
        } else {
            return UNDEFINED;
        }
    }

    private static final long sibling(long currentIndex) {
        if (parent(currentIndex) == UNDEFINED) {
            return UNDEFINED;
        } else {
            if (currentIndex == left(parent(currentIndex))) {
                return right(parent(currentIndex));
            } else {
                return left(parent(currentIndex));
            }
        }
    }

    private static final long uncle(long currentIndex) {
        if (parent(currentIndex) != UNDEFINED) {
            return sibling(parent(currentIndex));
        } else {
            return UNDEFINED;
        }
    }

    private static final long previous(long p_index) {
        long p = p_index;
        if (left(p) != UNDEFINED) {
            p = left(p);
            while (right(p) != UNDEFINED) {
                p = right(p);
            }
            return p;
        } else {
            if (parent(p) != UNDEFINED) {
                if (p == right(parent(p))) {
                    return parent(p);
                } else {
                    while (parent(p) != UNDEFINED && p == left(parent(p))) {
                        p = parent(p);
                    }
                    return parent(p);
                }
            } else {
                return UNDEFINED;
            }
        }
    }

    /* Time never use direct lookup, sadly for performance, anyway this method is private to ensure the correctness of caching mechanism */
    public long lookup(long p_key) {
        long n = UNSAFE.getLong(_start_address + OFFSET_ROOT_INDEX);
        if (n == UNDEFINED) {
            return KConfig.NULL_LONG;
        }
        while (n != UNDEFINED) {
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
        while (indexEnd != UNDEFINED && key(indexEnd) >= startKey) {
            walker.elem(key(indexEnd));
            indexEnd = previous(indexEnd);
        }
    }

    protected long internal_previousOrEqual_index(long p_key) {
        long p = UNSAFE.getLong(_start_address + OFFSET_ROOT_INDEX);
        if (p == UNDEFINED) {
            return p;
        }
        while (p != UNDEFINED) {
            if (p_key == key(p)) {
                return p;
            }
            if (p_key > key(p)) {
                if (right(p) != UNDEFINED) {
                    p = right(p);
                } else {
                    return p;
                }
            } else {
                if (left(p) != UNDEFINED) {
                    p = left(p);
                } else {
                    long parent = parent(p);
                    long ch = p;
                    while (parent != UNDEFINED && ch == left(parent)) {
                        ch = parent;
                        parent = parent(parent);
                    }
                    return parent;
                }
            }
        }
        return UNDEFINED;
    }

    protected final long internal_lookup_value(long p_key) {
        long n = UNSAFE.getLong(_start_address + OFFSET_ROOT_INDEX);
        if (n == UNDEFINED) {
            return KConfig.NULL_LONG;
        }
        while (n != UNDEFINED) {
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

    private static final void rotateLeft(long n) {
        long r = right(n);
        replaceNode(n, r);
        setRight(n, left(r));
        if (left(r) != UNDEFINED) {
            setParent(left(r), n);
        }
        setLeft(r, n);
        setParent(n, r);
    }

    private static final void rotateRight(long n) {
        long l = left(n);
        replaceNode(n, l);
        setLeft(n, right(l));
        if (right(l) != UNDEFINED) {
            setParent(right(l), n);
        }
        setRight(l, n);
        setParent(n, l);
    }

    private static final void replaceNode(long oldn, long newn) {
        if (parent(oldn) == UNDEFINED) {
            UNSAFE.putLong(_start_address + OFFSET_ROOT_INDEX, newn);
        } else {
            if (oldn == left(parent(oldn))) {
                setLeft(parent(oldn), newn);
            } else {
                setRight(parent(oldn), newn);
            }
        }
        if (newn != UNDEFINED) {
            setParent(newn, parent(oldn));
        }
    }

    protected synchronized void internal_insert(long key, long value) {
        if ((size() + 1) > _threshold) {
            int length = (size() == 0 ? 1 : size() << 1);

            int size_base_segment = BASE_SEGMENT_LEN;
            int size_raw_segment = length * NODE_SIZE * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);

            _threshold = (int) (length * _loadFactor);
        }

        long insertedNodeIndex = size();
        if (insertedNodeIndex == 0) {
            UNSAFE.putInt(_start_address + OFFSET_SIZE, 1);

            setKey(insertedNodeIndex, key);
            if (NODE_SIZE == 6) {
                setValue(insertedNodeIndex, value);
            }
            setColor(insertedNodeIndex, 0);
            setLeft(insertedNodeIndex, -1);
            setRight(insertedNodeIndex, -1);
            setParent(insertedNodeIndex, -1);

            UNSAFE.putLong(_start_address + OFFSET_ROOT_INDEX, insertedNodeIndex);
        } else {
            long rootIndex = UNSAFE.getLong(_start_address + OFFSET_ROOT_INDEX);
            while (true) {
                if (key == key(rootIndex)) {
                    //nop _size
                    return;
                } else if (key < key(rootIndex)) {
                    if (left(rootIndex) == -1) {

                        setKey(insertedNodeIndex, key);
                        if (NODE_SIZE == 6) {
                            setValue(insertedNodeIndex, value);
                        }
                        setColor(insertedNodeIndex, 0);
                        setLeft(insertedNodeIndex, -1);
                        setRight(insertedNodeIndex, -1);
                        setParent(insertedNodeIndex, -1);

                        setLeft(rootIndex, insertedNodeIndex);

                        UNSAFE.putInt(_start_address + OFFSET_SIZE, size() + 1);
                        break;
                    } else {
                        rootIndex = left(rootIndex);
                    }
                } else {
                    if (right(rootIndex) == -1) {

                        setKey(insertedNodeIndex, key);
                        if (NODE_SIZE == 6) {
                            setValue(insertedNodeIndex, value);
                        }
                        setColor(insertedNodeIndex, 0);
                        setLeft(insertedNodeIndex, -1);
                        setRight(insertedNodeIndex, -1);
                        setParent(insertedNodeIndex, -1);

                        setRight(rootIndex, insertedNodeIndex);

                        UNSAFE.putInt(_start_address + OFFSET_SIZE, size() + 1);
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

    private static final void insertCase1(long n) {
        if (parent(n) == UNDEFINED) {
            setColor(n, 1);
        } else {
            insertCase2(n);
        }
    }

    private static final void insertCase2(long n) {
        if (nodeColor(parent(n)) == true) {
            return;
        } else {
            insertCase3(n);
        }
    }

    private static final void insertCase3(long n) {
        if (nodeColor(uncle(n)) == false) {
            setColor(parent(n), 1);
            setColor(uncle(n), 1);
            setColor(grandParent(n), 0);
            insertCase1(grandParent(n));
        } else {
            insertCase4(n);
        }
    }

    private static final void insertCase4(long n_n) {
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

    private static final void insertCase5(long n) {
        setColor(parent(n), 1);
        setColor(grandParent(n), 0);
        if (n == left(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateRight(grandParent(n));
        } else {
            rotateLeft(grandParent(n));
        }
    }

    public void delete(long p_key) {
        //TODO
    }

    private static final boolean nodeColor(long n) {
        if (n == UNDEFINED) {
            return true;
        } else {
            return color(n) == 1;
        }
    }

    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();
        long rootIndex = UNSAFE.getLong(_start_address + OFFSET_ROOT_INDEX);
        if (rootIndex == UNDEFINED) {
            builder.append("0");
        } else {
            builder.append(size());
            builder.append(',');
            int elemSize = NODE_SIZE;
            builder.append(key(rootIndex));
            for (int i = 0; i < size(); i++) {
                long nextNodeIndex = i; /*i * elemSize;*/
                long parentNodeIndex = parent(nextNodeIndex);
                boolean isOnLeft = false;
                if (parentNodeIndex != UNDEFINED) {
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
                //builder.append(key(nextNodeIndex));
                Base64.encodeToBuffer(key(nextNodeIndex), builder);
                builder.append(',');
                if (parentNodeIndex != UNDEFINED) {
                    //builder.append(beginParent / elemSize);
                    builder.append(key(parentNodeIndex));
                }
                if (elemSize > 5) {
                    builder.append(',');
                    //builder.append(value(nextNodeIndex));
                    Base64.encodeToBuffer(value(nextNodeIndex), builder);
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
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        int s = Integer.parseInt(payload.substring(initPos, cursor));
        internal_allocate(s);

        if (payload.charAt(cursor) == ',') {//className to parse
            UNSAFE.putInt(_start_address + OFFSET_SIZE, s);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        UNSAFE.putLong(_start_address + OFFSET_ROOT_INDEX, Integer.parseInt(payload.substring(initPos, cursor)));
        UNSAFE.setMemory(_start_address + OFFSET_BACK, internal_size_raw_segment(s), (byte) UNDEFINED);

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
                //long loopKey = Long.parseLong(payload.substring(beginChunk, cursor));
                long loopKey = Base64.decodeWithBounds(payload, beginChunk, cursor);
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
                        //long currentValue = Long.parseLong(payload.substring(beginChunk, cursor));
                        long currentValue = Base64.decodeWithBounds(payload, beginChunk, cursor);
                        setValue(_back_index, currentValue);
                    }
                }
                _back_index++;
            }
        }
    }

    public boolean isDirty() {
        return UNSAFE.getByte(_start_address + OFFSET_DIRTY) != 0;
    }

    public void setClean(KMetaModel p_metaModel) {
        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 0);
    }

    public void setDirty() {
        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 1);
    }

    public int counter() {
        return UNSAFE.getInt(_start_address + OFFSET_COUNTER);
    }

    public void inc() {
        int c = UNSAFE.getInt(_start_address + OFFSET_COUNTER);
        UNSAFE.putInt(_start_address + OFFSET_COUNTER, c + 1);
    }

    public void dec() {
        int c = UNSAFE.getInt(_start_address + OFFSET_COUNTER);
        UNSAFE.putInt(_start_address + OFFSET_COUNTER, c - 1);
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
