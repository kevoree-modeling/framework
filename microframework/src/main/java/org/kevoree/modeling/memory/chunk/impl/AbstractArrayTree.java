package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.KTreeWalker;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.Base64;
import org.kevoree.modeling.util.PrimitiveHelper;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractArrayTree implements KChunk {

    //constants definition
    private static final char BLACK_LEFT = '{';
    private static final char BLACK_RIGHT = '}';
    private static final char RED_LEFT = '[';
    private static final char RED_RIGHT = ']';
    private static final int META_SIZE = 3;
    private static final float LOAD_FACTOR = ((float) 75 / (float) 100);
    protected int kvSize = 1;
    private int _threshold = 0;
    //volatile variables
    private volatile int _root_index = -1;
    private volatile int _size = 0;
    private volatile InternalState state;
    //final local variables
    private final KChunkSpace _space;
    private final AtomicLong _flags;
    private final AtomicInteger _counter;
    protected volatile long _magic;

    //multi-thread sync
    private Random _random;
    private AtomicInteger _magicToken;

    public AbstractArrayTree(long p_universe, long p_time, long p_obj, KChunkSpace p_space) {
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);
        this._space = p_space;
        this._magic = PrimitiveHelper.rand();
        this._magicToken = new AtomicInteger(-1);
        this._random = new Random();
    }

    class InternalState {

        public InternalState(int[] _back_meta, long[] _back_kv, boolean[] _back_colors) {
            this._back_meta = _back_meta;
            this._back_kv = _back_kv;
            this._back_colors = _back_colors;
        }

        final int[] _back_meta;
        final long[] _back_kv;
        final boolean[] _back_colors;
    }

    public final int counter() {
        return this._counter.get();
    }

    public final int inc() {
        return this._counter.incrementAndGet();
    }

    public final int dec() {
        return this._counter.decrementAndGet();
    }

    private final long _universe;

    private final long _time;

    private final long _obj;

    public long universe() {
        return this._universe;
    }

    public long time() {
        return this._time;
    }

    public long obj() {
        return this._obj;
    }

    public long getFlags() {
        return _flags.get();
    }

    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!_flags.compareAndSet(val, nval));
    }

    public KChunkSpace space() {
        return _space;
    }

    private void allocate(int capacity) {
        state = new InternalState(new int[capacity * META_SIZE], new long[capacity * kvSize], new boolean[capacity]);
        _threshold = (int) (capacity * LOAD_FACTOR);
    }

    private void reallocate(int newCapacity) {
        _threshold = (int) (newCapacity * LOAD_FACTOR);
        long[] new_back_kv = new long[newCapacity * kvSize];
        if (state != null && state._back_kv != null) {
            System.arraycopy(state._back_kv, 0, new_back_kv, 0, _size * kvSize);
        }
        boolean[] new_back_colors = new boolean[newCapacity];
        if (state != null && state._back_colors != null) {
            System.arraycopy(state._back_colors, 0, new_back_colors, 0, _size);
            for (int i = _size; i < newCapacity; i++) {
                new_back_colors[i] = false;
            }
        }
        int[] new_back_meta = new int[newCapacity * META_SIZE];
        if (state != null && state._back_meta != null) {
            System.arraycopy(state._back_meta, 0, new_back_meta, 0, _size * META_SIZE);
            for (int i = _size * META_SIZE; i < newCapacity * META_SIZE; i++) {
                new_back_meta[i] = -1;
            }
        }
        state = new InternalState(new_back_meta, new_back_kv, new_back_colors);
    }

    public int size() {
        return _size;
    }

    protected final long key(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return state._back_kv[p_currentIndex * kvSize];
    }

    private void setKey(int p_currentIndex, long p_paramIndex) {
        state._back_kv[p_currentIndex * kvSize] = p_paramIndex;
    }

    protected final long value(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return state._back_kv[(p_currentIndex * kvSize) + 1];
    }

    private void setValue(int p_currentIndex, long p_paramIndex) {
        state._back_kv[(p_currentIndex * kvSize) + 1] = p_paramIndex;
    }

    private int left(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return state._back_meta[p_currentIndex * META_SIZE];
    }

    private void setLeft(int p_currentIndex, int p_paramIndex) {
        state._back_meta[p_currentIndex * META_SIZE] = p_paramIndex;
    }

    private int right(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return state._back_meta[(p_currentIndex * META_SIZE) + 1];
    }

    private void setRight(int p_currentIndex, int p_paramIndex) {
        state._back_meta[(p_currentIndex * META_SIZE) + 1] = p_paramIndex;
    }

    private int parent(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return state._back_meta[(p_currentIndex * META_SIZE) + 2];
    }

    private void setParent(int p_currentIndex, int p_paramIndex) {
        state._back_meta[(p_currentIndex * META_SIZE) + 2] = p_paramIndex;
    }

    private boolean color(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return true;
        }
        return state._back_colors[p_currentIndex];
    }

    private void setColor(int p_currentIndex, boolean p_paramIndex) {
        state._back_colors[p_currentIndex] = p_paramIndex;
    }

    private int grandParent(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        if (parent(p_currentIndex) != -1) {
            return parent(parent(p_currentIndex));
        } else {
            return -1;
        }
    }

    private int sibling(int p_currentIndex) {
        if (parent(p_currentIndex) == -1) {
            return -1;
        } else {
            if (p_currentIndex == left(parent(p_currentIndex))) {
                return right(parent(p_currentIndex));
            } else {
                return left(parent(p_currentIndex));
            }
        }
    }

    private int uncle(int p_currentIndex) {
        if (parent(p_currentIndex) != -1) {
            return sibling(parent(p_currentIndex));
        } else {
            return -1;
        }
    }

    private int previous(int p_index) {
        int p = p_index;
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

    private int next(int p_index) {
        int p = p_index;
        if (right(p) != -1) {
            p = right(p);
            while (left(p) != -1) {
                p = left(p);
            }
            return p;
        } else {
            if (parent(p) != -1) {
                if (p == left(parent(p))) {
                    return parent(p);
                } else {
                    while (parent(p) != -1 && p == right(parent(p))) {
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
    public final long lookup(long p_key) {
        int n = _root_index;
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

    public final void range(long startKey, long endKey, KTreeWalker walker) {
        int indexEnd = internal_previousOrEqual_index(endKey);
        while (indexEnd != -1 && key(indexEnd) >= startKey) {
            walker.elem(key(indexEnd));
            indexEnd = previous(indexEnd);
        }
    }

    protected final int internal_previousOrEqual_index(long p_key) {

        //negociate a magic
        int newMagic;
        do {
            newMagic = _random.nextInt();
        } while (!this._magicToken.compareAndSet(-1, newMagic));

        int p = _root_index;
        if (p == -1) {
            //free magic
            this._magicToken.compareAndSet(newMagic, -1);
            return p;
        }

        while (p != -1) {
            if (p_key == key(p)) {
                //free magic
                this._magicToken.compareAndSet(newMagic, -1);
                return p;
            }
            if (p_key > key(p)) {
                if (right(p) != -1) {
                    p = right(p);
                } else {
                    //free magic
                    this._magicToken.compareAndSet(newMagic, -1);
                    return p;
                }
            } else {
                if (left(p) != -1) {
                    p = left(p);
                } else {
                    int parent = parent(p);
                    long ch = p;
                    while (parent != -1 && ch == left(parent)) {
                        ch = parent;
                        parent = parent(parent);
                    }
                    //free magic
                    this._magicToken.compareAndSet(newMagic, -1);
                    return parent;
                }
            }
        }

        //free magic
        this._magicToken.compareAndSet(newMagic, -1);
        return -1;
    }

    /* TODO manage with compare and swap here */
    private void rotateLeft(int n) {
        int r = right(n);
        replaceNode(n, r);
        setRight(n, left(r));
        if (left(r) != -1) {
            setParent(left(r), n);
        }
        setLeft(r, n);
        setParent(n, r);
    }

    private void rotateRight(int n) {
        int l = left(n);
        replaceNode(n, l);
        setLeft(n, right(l));
        if (right(l) != -1) {
            setParent(right(l), n);
        }
        setRight(l, n);
        setParent(n, l);
    }

    private void replaceNode(int oldn, int newn) {
        if (parent(oldn) == -1) {
            _root_index = newn;
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

    private void insertCase1(int n) {
        if (parent(n) == -1) {
            setColor(n, true);
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(int n) {
        if (!color(parent(n))) {
            insertCase3(n);
        }
    }

    private void insertCase3(int n) {
        if (!color(uncle(n))) {
            setColor(parent(n), true);
            setColor(uncle(n), true);
            setColor(grandParent(n), false);
            insertCase1(grandParent(n));
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(int n_n) {
        int n = n_n;
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

    private void insertCase5(int n) {
        setColor(parent(n), true);
        setColor(grandParent(n), false);
        if (n == left(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateRight(grandParent(n));
        } else {
            rotateLeft(grandParent(n));
        }
    }

    public final String serialize(KMetaModel metaModel) {

        //negociate a magic
        int newMagic;
        do {
            newMagic = _random.nextInt();
        } while (!this._magicToken.compareAndSet(-1, newMagic));

        if (_root_index == -1) {
            //free magic
            this._magicToken.compareAndSet(newMagic, -1);
            return "0";
        }

        int savedRoot = _root_index;
        InternalState internalState = state;
        StringBuilder builder = new StringBuilder();
        int treeSize = 0;
        for (int i = 0; i < internalState._back_meta.length / META_SIZE; i++) {
            int parentIndex = internalState._back_meta[(i * META_SIZE) + 2];
            if (parentIndex != -1 || i == savedRoot) {
                boolean isOnLeft = false;
                if (parentIndex != -1) {
                    isOnLeft = internalState._back_meta[parentIndex * META_SIZE] == i;
                }
                if (!color(i)) {
                    if (isOnLeft) {
                        builder.append(BLACK_LEFT);
                    } else {
                        builder.append(BLACK_RIGHT);
                    }
                } else {//red
                    if (isOnLeft) {
                        builder.append(RED_LEFT);
                    } else {
                        builder.append(RED_RIGHT);
                    }
                }
                Base64.encodeLongToBuffer(internalState._back_kv[i * kvSize], builder);
                builder.append(',');
                if (parentIndex != -1) {
                    Base64.encodeIntToBuffer(parentIndex, builder);
                }
                if (kvSize > 1) {
                    builder.append(',');
                    Base64.encodeLongToBuffer(internalState._back_kv[(i * kvSize) + 1], builder);
                }
                treeSize++;
            }
        }
        String result = Base64.encodeInt(treeSize) + "," + Base64.encodeInt(savedRoot) + builder.toString();
        //free magic
        this._magicToken.compareAndSet(newMagic, -1);
        return result;
    }

    public final void init(String payload, KMetaModel metaModel, int metaClassIndex) {
        if (payload == null || payload.length() == 0) {
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }
        if (payload.charAt(cursor) == ',') {//className to parse
            _size = Base64.decodeToIntWithBounds(payload, initPos, cursor);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }
        _root_index = Base64.decodeToIntWithBounds(payload, initPos, cursor);
        allocate(_size);
        for (int i = 0; i < _size; i++) {
            int offsetI = i * META_SIZE;
            state._back_meta[offsetI] = -1;
            state._back_meta[offsetI + 1] = -1;
            state._back_meta[offsetI + 2] = -1;
        }
        int currentLoopIndex = 0;
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
                    setColor(currentLoopIndex, false);
                } else {
                    setColor(currentLoopIndex, true);
                }
                cursor++;
                int beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                    cursor++;
                }
                long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, cursor);
                setKey(currentLoopIndex, loopKey);
                cursor++;
                beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    int parentRaw = Base64.decodeToIntWithBounds(payload, beginChunk, cursor);
                    setParent(currentLoopIndex, parentRaw);
                    if (isOnLeft) {
                        setLeft(parentRaw, currentLoopIndex);
                    } else {
                        setRight(parentRaw, currentLoopIndex);
                    }
                }
                if (cursor < payload.length() && payload.charAt(cursor) == ',') {
                    cursor++;
                    beginChunk = cursor;
                    while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                        cursor++;
                    }
                    if (cursor > beginChunk) {
                        long currentValue = Base64.decodeToLongWithBounds(payload, beginChunk, cursor);
                        setValue(currentLoopIndex, currentValue);
                    }
                }
                currentLoopIndex++;
            }
        }
    }

    public final void free(KMetaModel p_metaModel) {
        this.state = null;
        this._size = 0;
        this._threshold = 0;
    }

    protected final void internal_insert(long p_key, long p_value) {

        //negociate a magic
        int newMagic;
        do {
            newMagic = _random.nextInt();
        } while (!this._magicToken.compareAndSet(-1, newMagic));

        if ((_size + 1) > _threshold) {
            int length = (_size == 0 ? 1 : _size << 1);
            reallocate(length);
        }
        int newIndex = _size;
        if (newIndex == 0) {
            setKey(newIndex, p_key);
            if (kvSize == 2) {
                setValue(newIndex, p_value);
            }
            setColor(newIndex, false);
            setLeft(newIndex, -1);
            setRight(newIndex, -1);
            setParent(newIndex, -1);
            _root_index = newIndex;
            _size = 1;
        } else {
            int n = _root_index;
            while (true) {
                if (p_key == key(n)) {
                    //nop _size

                    //free magic
                    this._magicToken.compareAndSet(newMagic, -1);
                    return;
                } else if (p_key < key(n)) {
                    if (left(n) == -1) {
                        setKey(newIndex, p_key);
                        if (kvSize == 2) {
                            setValue(newIndex, p_value);
                        }
                        setColor(newIndex, false);
                        setLeft(newIndex, -1);
                        setRight(newIndex, -1);
                        setParent(newIndex, -1);
                        setLeft(n, newIndex);
                        _size++;
                        break;
                    } else {
                        n = left(n);
                    }
                } else {
                    if (right(n) == -1) {
                        setKey(newIndex, p_key);
                        if (kvSize == 2) {
                            setValue(newIndex, p_value);
                        }
                        setColor(newIndex, false);
                        setLeft(newIndex, -1);
                        setRight(newIndex, -1);
                        setParent(newIndex, -1);
                        setRight(n, newIndex);
                        _size++;
                        break;
                    } else {
                        n = right(n);
                    }
                }
            }
            setParent(newIndex, n);
        }
        insertCase1(newIndex);
        internal_set_dirty();

        //free magic
        this._magicToken.compareAndSet(newMagic, -1);

    }

    protected final long internal_lookup_value(long p_key) {

        //negociate a magic
        int newMagic;
        do {
            newMagic = _random.nextInt();
        } while (!this._magicToken.compareAndSet(-1, newMagic));

        int n = _root_index;
        if (n == -1) {
            //free magic
            this._magicToken.compareAndSet(newMagic, -1);
            return KConfig.NULL_LONG;
        }
        while (n != -1) {
            if (p_key == key(n)) {
                //free magic
                this._magicToken.compareAndSet(newMagic, -1);
                return value(n);
            } else {
                if (p_key < key(n)) {
                    n = left(n);
                } else {
                    n = right(n);
                }
            }
        }
        //free magic
        this._magicToken.compareAndSet(newMagic, -1);
        return n;
    }

    private void internal_set_dirty() {
        if (_space != null) {
            if ((_flags.get() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.declareDirty(this);
                //the synchronization risk is minim here, at worse the object will be saved twice for the next iteration
                setFlags(KChunkFlags.DIRTY_BIT, 0);
            }
        } else {
            setFlags(KChunkFlags.DIRTY_BIT, 0);
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

    public abstract short type();

}
