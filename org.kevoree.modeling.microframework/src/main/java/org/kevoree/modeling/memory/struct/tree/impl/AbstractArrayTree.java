package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.util.maths.Base64;

public abstract class AbstractArrayTree {

    protected long _root_index = -1;
    protected int _size = 0;
    protected int _threshold = 0;
    protected float _loadFactor;
    protected long[] _back = null;
    protected long[] _back = null;

    private boolean _dirty = true;
    private int _counter = 0;

    public AbstractArrayTree() {
        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
    }

    abstract int ELEM_SIZE();

    private void allocate(int capacity) {
        _back = new long[capacity * ELEM_SIZE()];
        _threshold = (int) (capacity * _loadFactor);
    }

    public int size() {
        return _size;
    }

    protected long key(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back[(int) p_currentIndex];
    }

    protected void setKey(long p_currentIndex, long p_paramIndex) {
        _back[(int) p_currentIndex] = p_paramIndex;
    }

    protected long left(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back[(int) p_currentIndex + 1];
    }

    protected void setLeft(long p_currentIndex, long p_paramIndex) {
        _back[(int) p_currentIndex + 1] = p_paramIndex;
    }

    protected long right(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back[(int) p_currentIndex + 2];
    }

    protected void setRight(long p_currentIndex, long p_paramIndex) {
        _back[(int) p_currentIndex + 2] = p_paramIndex;
    }

    private long parent(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back[(int) p_currentIndex + 3];
    }

    protected void setParent(long p_currentIndex, long p_paramIndex) {
        _back[(int) p_currentIndex + 3] = p_paramIndex;
    }

    private long color(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return _back[(int) currentIndex + 4];
    }

    protected void setColor(long currentIndex, long paramIndex) {
        _back[(int) currentIndex + 4] = paramIndex;
    }

    protected long value(long currentIndex) {
        if (currentIndex == -1) {
            return -1;
        }
        return _back[(int) currentIndex + 5];
    }

    protected void setValue(long currentIndex, long paramIndex) {
        _back[(int) currentIndex + 5] = paramIndex;
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
        long n = _root_index;
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
        long p = _root_index;
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

    private boolean nodeColor(long n) {
        if (n == -1) {
            return true;
        } else {
            return color(n) == 1;
        }
    }

    private static final char BLACK_LEFT = '{';
    private static final char BLACK_RIGHT = '}';
    private static final char RED_LEFT = '[';
    private static final char RED_RIGHT = ']';


    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();
        if (_root_index == -1) {
            builder.append("0");
        } else {
            builder.append(_size);
            builder.append(',');
            int elemSize = ELEM_SIZE();
            builder.append(_root_index / elemSize);
            for (int i = 0; i < _size; i++) {
                int nextSegmentBegin = i * elemSize;
                long beginParent = parent(nextSegmentBegin);
                boolean isOnLeft = false;
                if (beginParent != -1) {
                    isOnLeft = left(beginParent) == nextSegmentBegin;
                }
                if (color(nextSegmentBegin) == 0) {
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
                //builder.append(key(nextSegmentBegin));
                Base64.encodeToBuffer(key(nextSegmentBegin), builder);
                builder.append(',');
                if (beginParent != -1) {
                    builder.append(beginParent / elemSize);
                }
                if (elemSize > 5) {
                    builder.append(',');
                    //builder.append(value(nextSegmentBegin));
                    Base64.encodeToBuffer(value(nextSegmentBegin), builder);
                }
            }
        }
        return builder.toString();
    }

    public void init(String payload, KMetaModel metaModel) {
        if (payload == null || payload.length() == 0) {
            return;
        }
        int elemSize = ELEM_SIZE();
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }
        if (payload.charAt(cursor) == ',') {//className to parse
            _size = Integer.parseInt(payload.substring(initPos, cursor));
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }
        _root_index = Integer.parseInt(payload.substring(initPos, cursor)) * elemSize;
        allocate(_size);
        for (int i = 0; i < _size * elemSize; i++) {
            _back[i] = -1;
        }
        int _back_index = 0;
        while (cursor < payload.length()) {
            while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                cursor++;
            }
            if (cursor < payload.length()) {
                char elem = payload.charAt(cursor);
                int currentBlock = _back_index * elemSize;
                boolean isOnLeft = false;
                if (elem == BLACK_LEFT || elem == RED_LEFT) {
                    isOnLeft = true;
                }
                if (elem == BLACK_LEFT || elem == BLACK_RIGHT) {
                    setColor(currentBlock, 0);
                } else {
                    setColor(currentBlock, 1);
                }
                cursor++;
                int beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                    cursor++;
                }
                //long loopKey = Long.parseLong(payload.substring(beginChunk, cursor));
                long loopKey = Base64.decodeWithBounds(payload, beginChunk, cursor);
                setKey(currentBlock, loopKey);
                cursor++;
                beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    long parentRaw = Long.parseLong(payload.substring(beginChunk, cursor));
                    long parentValue = parentRaw * elemSize;
                    setParent(currentBlock, parentValue);
                    if (isOnLeft) {
                        setLeft(parentValue, currentBlock);
                    } else {
                        setRight(parentValue, currentBlock);
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
                        setValue(currentBlock, currentValue);
                    }
                }
                _back_index++;
            }
        }
    }

    public boolean isDirty() {
        return _dirty;
    }

    public void setClean(KMetaModel p_metaModel) {
        _dirty = false;
    }

    public void setDirty() {
        _dirty = true;
    }

    public int counter() {
        return this._counter;
    }

    public void inc() {
        this._counter--;
    }

    public void dec() {
        this._counter--;
    }

    public void free(KMetaModel p_metaModel) {
        this._back = null;
        this._threshold = 0;
    }

}
