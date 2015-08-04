package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KTreeWalker;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;
import sun.misc.Unsafe;

/**
 * @ignore ts
 * <p/>
 * OffHeap implementation of AbstractOffHeapTree
 * - memory structure:  | root index (8) | size (4) | flags (8) | counter (4) | back (size * node size * 8) |
 * - back:              | key (8) | left (8) | right (8) | parent (8) | color (8) | value (8) |
 */
public abstract class AbstractOffHeapTree implements KOffHeapChunk {
    protected static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    protected OffHeapChunkSpace _space;
    protected long _universe, _time, _obj;

    private volatile long _start_address;
    protected int threshold;
    protected float loadFactor;

    protected int NODE_SIZE;

    // constants for tree semantic
    private static final char BLACK_LEFT = '{';
    private static final char BLACK_RIGHT = '}';
    private static final char RED_LEFT = '[';
    private static final char RED_RIGHT = ']';

    // constants for off-heap memory layout
    private static final long UNDEFINED = -1;
    private static final int BYTE = 8;

    private static final int POS_KEY = 0;
    private static final int POS_LEFT = 1;
    private static final int POS_RIGHT = 2;
    private static final int POS_PARENT = 3;
    private static final int POS_COLOR = 4;
    private static final int POS_VALUE = 5;

    private static final int ATT_ROOT_INDEX_LEN = 8;
    private static final int ATT_SIZE_LEN = 4;
    private static final int ATT_FLAGS_LEN = 8;
    private static final int ATT_COUNTER_LEN = 4;

    private static final int OFFSET_ROOT_INDEX = 0;
    private static final int OFFSET_SIZE = OFFSET_ROOT_INDEX + ATT_ROOT_INDEX_LEN;
    private static final int OFFSET_FLAGS = OFFSET_SIZE + ATT_SIZE_LEN;
    private static final int OFFSET_COUNTER = OFFSET_FLAGS + ATT_FLAGS_LEN;
    private static final int OFFSET_BACK = OFFSET_COUNTER + ATT_COUNTER_LEN;

    private static final int BASE_SEGMENT_LEN = ATT_ROOT_INDEX_LEN + ATT_SIZE_LEN + ATT_FLAGS_LEN + ATT_COUNTER_LEN;

    protected AbstractOffHeapTree() {
        NODE_SIZE = 0;
    }

    private final void allocate(int p_length) {
        long bytes = BASE_SEGMENT_LEN + sizeOfRawSegment(p_length);

        this._start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(this._start_address, bytes, (byte) 0);

        UNSAFE.putLong(this._start_address + OFFSET_ROOT_INDEX, UNDEFINED);
        UNSAFE.putInt(this._start_address + OFFSET_SIZE, p_length);

        this.loadFactor = KConfig.CACHE_LOAD_FACTOR;
        this.threshold = (int) (size() * this.loadFactor);

        if (_space != null) {
            _space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
        }
    }

    private void reallocate(int p_length) {
        int size_base_segment = BASE_SEGMENT_LEN;
        int size_raw_segment = p_length * NODE_SIZE * BYTE;
        long newAddress = UNSAFE.allocateMemory(size_base_segment + size_raw_segment);
        UNSAFE.copyMemory(this._start_address, newAddress, BASE_SEGMENT_LEN + size() * NODE_SIZE * BYTE);
        long oldAddress = this._start_address;
        this._start_address = newAddress;
        UNSAFE.freeMemory(oldAddress);

        this.threshold = (int) (p_length * this.loadFactor);

        if (_space != null) {
            _space.notifyRealloc(this._start_address, this._universe, this._time, this._obj);
        }
    }

    private int sizeOfRawSegment(int p_length) {
        return p_length * BYTE * NODE_SIZE;
    }

    public final int size() {
        return UNSAFE.getInt(this._start_address + OFFSET_SIZE);
    }

    protected final long key(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        return UNSAFE.getLong(addr + POS_KEY * BYTE);
    }

    private void setKey(long p_nodeIndex, long p_key) {
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        UNSAFE.putLong(addr + POS_KEY * BYTE, p_key);
    }

    private long left(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        return UNSAFE.getLong(addr + POS_LEFT * BYTE);
    }

    private void setLeft(long p_nodeIndex, long p_leftNodeIndex) {
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        UNSAFE.putLong(addr + POS_LEFT * BYTE, p_leftNodeIndex);
    }

    private long right(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        return UNSAFE.getLong(addr + POS_RIGHT * BYTE);
    }

    private void setRight(long p_nodeIndex, long p_rightNodeIndex) {
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        UNSAFE.putLong(addr + POS_RIGHT * BYTE, p_rightNodeIndex);
    }

    private long parent(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        long address = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        return UNSAFE.getLong(address + POS_PARENT * BYTE);
    }

    private void setParent(long p_nodeIndex, long p_parentNodeIndex) {
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        UNSAFE.putLong(addr + POS_PARENT * BYTE, p_parentNodeIndex);
    }

    private long color(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        return UNSAFE.getLong(addr + POS_COLOR * BYTE);
    }

    private void setColor(long p_nodeIndex, long p_color) {
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        UNSAFE.putLong(addr + POS_COLOR * BYTE, p_color);
    }

    protected final long value(long p_nodeIndex) {
        if (p_nodeIndex == UNDEFINED) {
            return UNDEFINED;
        }
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        return UNSAFE.getLong(addr + POS_VALUE * BYTE);
    }

    private void setValue(long p_nodeIndex, long p_value) {
        long addr = this._start_address + OFFSET_BACK + p_nodeIndex * BYTE * NODE_SIZE;
        UNSAFE.putLong(addr + POS_VALUE * BYTE, p_value);
    }

    private long grandParent(long p_currentIndex) {
        if (p_currentIndex == UNDEFINED) {
            return UNDEFINED;
        }
        if (parent(p_currentIndex) != UNDEFINED) {
            return parent(parent(p_currentIndex));
        } else {
            return UNDEFINED;
        }
    }

    private long sibling(long p_currentIndex) {
        if (parent(p_currentIndex) == UNDEFINED) {
            return UNDEFINED;
        } else {
            if (p_currentIndex == left(parent(p_currentIndex))) {
                return right(parent(p_currentIndex));
            } else {
                return left(parent(p_currentIndex));
            }
        }
    }

    private long uncle(long p_currentIndex) {
        if (parent(p_currentIndex) != UNDEFINED) {
            return sibling(parent(p_currentIndex));
        } else {
            return UNDEFINED;
        }
    }

    private long previous(long p_index) {
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

    private long next(long p_index) {
        long p = p_index;
        if (right(p) != UNDEFINED) {
            p = right(p);
            while (left(p) != UNDEFINED) {
                p = left(p);
            }
            return p;
        } else {
            if (parent(p) != UNDEFINED) {
                if (p == left(parent(p))) {
                    return parent(p);
                } else {
                    while (parent(p) != UNDEFINED && p == right(parent(p))) {
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
    public final long lookup(long p_key) {
        long n = UNSAFE.getLong(this._start_address + OFFSET_ROOT_INDEX);
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

    public final void range(long p_startKey, long p_endKey, KTreeWalker p_walker) {
        long indexEnd = previousOrEqualIndex(p_endKey);
        while (indexEnd != UNDEFINED && key(indexEnd) >= p_startKey) {
            p_walker.elem(key(indexEnd));
            indexEnd = previous(indexEnd);
        }
    }

    protected final long previousOrEqualIndex(long p_key) {
        long p = UNSAFE.getLong(this._start_address + OFFSET_ROOT_INDEX);
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

    protected final long internal_lookupValue(long p_key) {
        long n = UNSAFE.getLong(this._start_address + OFFSET_ROOT_INDEX);
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

    private void rotateLeft(long n) {
        long r = right(n);
        replaceNode(n, r);
        setRight(n, left(r));
        if (left(r) != UNDEFINED) {
            setParent(left(r), n);
        }
        setLeft(r, n);
        setParent(n, r);
    }

    private void rotateRight(long n) {
        long l = left(n);
        replaceNode(n, l);
        setLeft(n, right(l));
        if (right(l) != UNDEFINED) {
            setParent(right(l), n);
        }
        setRight(l, n);
        setParent(n, l);
    }

    private void replaceNode(long oldn, long newn) {
        if (parent(oldn) == UNDEFINED) {
            UNSAFE.putLong(this._start_address + OFFSET_ROOT_INDEX, newn);
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
        if ((size() + 1) > this.threshold) {
            int length = (size() == 0 ? 1 : size() << 1);

            reallocate(length);
        }

        long insertedNodeIndex = size();
        if (insertedNodeIndex == 0) {
            UNSAFE.putInt(this._start_address + OFFSET_SIZE, 1);

            setKey(insertedNodeIndex, key);
            if (NODE_SIZE == 6) {
                setValue(insertedNodeIndex, value);
            }
            setColor(insertedNodeIndex, 0);
            setLeft(insertedNodeIndex, -1);
            setRight(insertedNodeIndex, -1);
            setParent(insertedNodeIndex, -1);

            UNSAFE.putLong(this._start_address + OFFSET_ROOT_INDEX, insertedNodeIndex);
        } else {
            long rootIndex = UNSAFE.getLong(this._start_address + OFFSET_ROOT_INDEX);
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

                        UNSAFE.putInt(this._start_address + OFFSET_SIZE, size() + 1);
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

                        UNSAFE.putInt(this._start_address + OFFSET_SIZE, size() + 1);
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

    private void insertCase1(long n) {
        if (parent(n) == UNDEFINED) {
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

    public final void delete(long p_key) {
        //TODO
    }

    private boolean nodeColor(long n) {
        if (n == UNDEFINED) {
            return true;
        } else {
            return color(n) == 1;
        }
    }

    public final String serialize(KMetaModel p_metaModel) {
        StringBuilder builder = new StringBuilder();
        long rootIndex = UNSAFE.getLong(this._start_address + OFFSET_ROOT_INDEX);
        if (rootIndex == UNDEFINED) {
            builder.append("0");
        } else {
            Base64.encodeIntToBuffer(size(), builder);
            builder.append(',');
            Base64.encodeLongToBuffer(rootIndex, builder);
            int elemSize = NODE_SIZE;
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
                Base64.encodeLongToBuffer(key(nextNodeIndex), builder);
                builder.append(',');
                if (parentNodeIndex != UNDEFINED) {
                    Base64.encodeLongToBuffer(parentNodeIndex, builder);
                }
                if (elemSize > 5) {
                    builder.append(',');
                    Base64.encodeLongToBuffer(value(nextNodeIndex), builder);
                }
            }
        }
        return builder.toString();
    }

    public final void init(String p_payload, KMetaModel p_metaModel, int p_metaClassIndex) {
        if (p_payload == null || p_payload.length() == 0) {
            allocate(0);
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < p_payload.length() && p_payload.charAt(cursor) != ',' && p_payload.charAt(cursor) != BLACK_LEFT && p_payload.charAt(cursor) != BLACK_RIGHT && p_payload.charAt(cursor) != RED_LEFT && p_payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        int s = Base64.decodeToIntWithBounds(p_payload, initPos, cursor);
        allocate(s);

        if (p_payload.charAt(cursor) == ',') {//className to parse
            UNSAFE.putInt(this._start_address + OFFSET_SIZE, s);
            cursor++;
            initPos = cursor;
        }
        while (cursor < p_payload.length() && p_payload.charAt(cursor) != BLACK_LEFT && p_payload.charAt(cursor) != BLACK_RIGHT && p_payload.charAt(cursor) != RED_LEFT && p_payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        UNSAFE.putLong(this._start_address + OFFSET_ROOT_INDEX, Base64.decodeToIntWithBounds(p_payload, initPos, cursor));
        UNSAFE.setMemory(this._start_address + OFFSET_BACK, sizeOfRawSegment(s), (byte) UNDEFINED);

        int _back_index = 0;
        while (cursor < p_payload.length()) {
            while (cursor < p_payload.length() && p_payload.charAt(cursor) != BLACK_LEFT && p_payload.charAt(cursor) != BLACK_RIGHT && p_payload.charAt(cursor) != RED_LEFT && p_payload.charAt(cursor) != RED_RIGHT) {
                cursor++;
            }
            if (cursor < p_payload.length()) {
                char elem = p_payload.charAt(cursor);

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
                while (cursor < p_payload.length() && p_payload.charAt(cursor) != ',') {
                    cursor++;
                }

                long loopKey = Base64.decodeToLongWithBounds(p_payload, beginChunk, cursor);
                setKey(_back_index, loopKey);
                cursor++;
                beginChunk = cursor;
                while (cursor < p_payload.length() && p_payload.charAt(cursor) != ',' && p_payload.charAt(cursor) != BLACK_LEFT && p_payload.charAt(cursor) != BLACK_RIGHT && p_payload.charAt(cursor) != RED_LEFT && p_payload.charAt(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    long parentValue = Base64.decodeToLongWithBounds(p_payload, beginChunk, cursor);
                    setParent(_back_index, parentValue);
                    if (isOnLeft) {
                        setLeft(parentValue, _back_index);
                    } else {
                        setRight(parentValue, _back_index);
                    }
                }
                if (cursor < p_payload.length() && p_payload.charAt(cursor) == ',') {
                    cursor++;
                    beginChunk = cursor;
                    while (cursor < p_payload.length() && p_payload.charAt(cursor) != BLACK_LEFT && p_payload.charAt(cursor) != BLACK_RIGHT && p_payload.charAt(cursor) != RED_LEFT && p_payload.charAt(cursor) != RED_RIGHT) {
                        cursor++;
                    }
                    if (cursor > beginChunk) {
                        long currentValue = Base64.decodeToLongWithBounds(p_payload, beginChunk, cursor);
                        setValue(_back_index, currentValue);
                    }
                }
                _back_index++;
            }
        }
    }

    public final int counter() {
        return UNSAFE.getInt(this._start_address + OFFSET_COUNTER);
    }

    public final void inc() {
        int c = UNSAFE.getInt(this._start_address + OFFSET_COUNTER);
        UNSAFE.putInt(this._start_address + OFFSET_COUNTER, c + 1);
    }

    public final void dec() {
        int c = UNSAFE.getInt(this._start_address + OFFSET_COUNTER);
        UNSAFE.putInt(this._start_address + OFFSET_COUNTER, c - 1);
    }

    public final void free(KMetaModel p_metaModel) {
        UNSAFE.freeMemory(this._start_address);
    }

    @Override
    public final long memoryAddress() {
        return this._start_address;
    }

    @Override
    public final void setMemoryAddress(long address) {
        this._start_address = address;

        loadFactor = KConfig.CACHE_LOAD_FACTOR;
        this.threshold = (int) (size() * this.loadFactor);
    }

    @Override
    public KChunkSpace space() {
        return this._space;
    }

    @Override
    public long getFlags() {
        return UNSAFE.getLong(this._start_address + OFFSET_FLAGS);
    }

    @Override
    public void setFlags(long p_bitsToEnable, long p_bitsToDisable) {
        long expected;
        long updated;
        do {
            expected = UNSAFE.getLong(this._start_address + OFFSET_FLAGS);
            updated = expected & ~p_bitsToDisable | p_bitsToEnable;
        } while (!UNSAFE.compareAndSwapLong(this, this._start_address + OFFSET_FLAGS, expected, updated));
    }

    @Override
    public long universe() {
        return this._universe;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long obj() {
        return this._obj;
    }
}
