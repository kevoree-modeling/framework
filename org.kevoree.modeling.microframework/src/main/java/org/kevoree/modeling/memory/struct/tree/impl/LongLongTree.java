package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.memory.struct.tree.KLongLongTree;

public class LongLongTree implements KMemoryElement, KLongLongTree {

    private LongTreeNode root = null;

    private int _size = 0;

    public int size() {
        return _size;
    }

    public boolean _dirty = false;

    private int _counter = 0;

    @Override
    public int counter() {
        return _counter;
    }

    @Override
    public void inc() {
        _counter++;
    }

    @Override
    public void dec() {
        _counter--;
    }

    @Override
    public void free(KMetaModel metaModel) {
        //NOOP
    }

    @Override
    public String toString() {
        return serialize(null);
    }

    @Override
    public boolean isDirty() {
        return _dirty;
    }

    @Override
    public void setDirty() {
        this._dirty = true;
    }

    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();
        builder.append(_size);
        if (root != null) {
            root.serialize(builder);
        }
        return builder.toString();
    }


    /*
    *
    *   builder.append("|");
        if (color == true) {
            builder.append(BLACK);
        } else {
            builder.append(RED);
        }
        builder.append(key);
        builder.append("@");
        builder.append(value);
        if (left == null && right == null) {
            builder.append("%");
        } else {
            if (left != null) {
                left.serialize(builder);
            } else {
                builder.append("#");
            }
            if (right != null) {
                right.serialize(builder);
            } else {
                builder.append("#");
            }
        }
    *
    * */


    private LongTreeNode[] _previousOrEqualsCacheValues = null;
    private int _previousOrEqualsNextCacheElem;

    private LongTreeNode[] _lookupCacheValues = null;
    private int _lookupNextCacheElem;

    public LongLongTree() {
        _lookupCacheValues = new LongTreeNode[KConfig.TREE_CACHE_SIZE];
        _previousOrEqualsCacheValues = new LongTreeNode[KConfig.TREE_CACHE_SIZE];
        _previousOrEqualsNextCacheElem = 0;
        _lookupNextCacheElem = 0;
    }

    /* Cache management */
    private LongTreeNode tryPreviousOrEqualsCache(long key) {
        if (_previousOrEqualsCacheValues != null) {
            for (int i = 0; i < _previousOrEqualsNextCacheElem; i++) {
                if (_previousOrEqualsCacheValues[i] != null && key == _previousOrEqualsCacheValues[i].key) {
                    return _previousOrEqualsCacheValues[i];
                }
            }
        }
        return null;
    }

    private LongTreeNode tryLookupCache(long key) {
        if (_lookupCacheValues != null) {
            for (int i = 0; i < _lookupNextCacheElem; i++) {
                if (_lookupCacheValues[i] != null && key == _lookupCacheValues[i].key) {
                    return _lookupCacheValues[i];
                }
            }
        }
        return null;
    }

    private void resetCache() {
        _previousOrEqualsNextCacheElem = 0;
        _lookupNextCacheElem = 0;
    }

    private void putInPreviousOrEqualsCache(LongTreeNode resolved) {
        if (_previousOrEqualsNextCacheElem == KConfig.TREE_CACHE_SIZE) {
            _previousOrEqualsNextCacheElem = 0;
        }
        _previousOrEqualsCacheValues[_previousOrEqualsNextCacheElem] = resolved;
        _previousOrEqualsNextCacheElem++;
    }

    private void putInLookupCache(LongTreeNode resolved) {
        if (_lookupNextCacheElem == KConfig.TREE_CACHE_SIZE) {
            _lookupNextCacheElem = 0;
        }
        _lookupCacheValues[_lookupNextCacheElem] = resolved;
        _lookupNextCacheElem++;
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        this._dirty = false;
    }

    @Override
    public void init(String payload, KMetaModel metaModel) throws Exception {
        if (payload == null || payload.length() == 0) {
            return;
        }
        int i = 0;
        StringBuilder buffer = new StringBuilder();
        char ch = payload.charAt(i);
        while (i < payload.length() && ch != '|') {
            buffer.append(ch);
            i = i + 1;
            ch = payload.charAt(i);
        }
        _size = Integer.parseInt(buffer.toString());
        TreeReaderContext ctx = new TreeReaderContext();
        ctx.index = i;
        ctx.payload = payload;
        ctx.buffer = new char[20];
        root = LongTreeNode.unserialize(ctx);
        resetCache();
    }

    public long lookupValue(long key) {
        LongTreeNode result = internal_lookup(key);
        if (result != null) {
            return result.value;
        } else {
            return KConfig.NULL_LONG;
        }
    }

    private LongTreeNode internal_lookup(long key) {
        LongTreeNode n = tryLookupCache(key);
        if (n != null) {
            return n;
        }
        n = root;
        if (n == null) {
            return null;
        }
        while (n != null) {
            if (key == n.key) {
                putInLookupCache(n);
                return n;
            } else {
                if (key < n.key) {
                    n = n.getLeft();
                } else {
                    n = n.getRight();
                }
            }
        }
        putInLookupCache(null);
        return n;
    }

    public long previousOrEqualValue(long key) {
        LongTreeNode result = internal_previousOrEqual(key);
        if (result != null) {
            return result.value;
        } else {
            return KConfig.NULL_LONG;
        }
    }

    private LongTreeNode internal_previousOrEqual(long key) {
        LongTreeNode p = tryPreviousOrEqualsCache(key);
        if (p != null) {
            return p;
        }
        p = root;
        if (p == null) {
            return null;
        }
        while (p != null) {
            if (key == p.key) {
                putInPreviousOrEqualsCache(p);
                return p;
            }
            if (key > p.key) {
                if (p.getRight() != null) {
                    p = p.getRight();
                } else {
                    putInPreviousOrEqualsCache(p);
                    return p;
                }
            } else {
                if (p.getLeft() != null) {
                    p = p.getLeft();
                } else {
                    LongTreeNode parent = p.getParent();
                    LongTreeNode ch = p;
                    while (parent != null && ch == parent.getLeft()) {
                        ch = parent;
                        parent = parent.getParent();
                    }
                    putInPreviousOrEqualsCache(parent);
                    return parent;
                }
            }
        }
        return null;
    }

    public LongTreeNode nextOrEqual(long key) {
        LongTreeNode p = root;
        if (p == null) {
            return null;
        }
        while (p != null) {
            if (key == p.key) {
                return p;
            }
            if (key < p.key) {
                if (p.getLeft() != null) {
                    p = p.getLeft();
                } else {
                    return p;
                }
            } else {
                if (p.getRight() != null) {
                    p = p.getRight();
                } else {
                    LongTreeNode parent = p.getParent();
                    LongTreeNode ch = p;
                    while (parent != null && ch == parent.getRight()) {
                        ch = parent;
                        parent = parent.getParent();
                    }
                    return parent;
                }
            }
        }
        return null;
    }

    public LongTreeNode previous(long key) {
        LongTreeNode p = root;
        if (p == null) {
            return null;
        }
        while (p != null) {
            if (key < p.key) {
                if (p.getLeft() != null) {
                    p = p.getLeft();
                } else {
                    return p.previous();
                }
            } else if (key > p.key) {
                if (p.getRight() != null) {
                    p = p.getRight();
                } else {
                    return p;
                }
            } else {
                return p.previous();
            }
        }
        return null;
    }

    public LongTreeNode next(long key) {
        LongTreeNode p = root;
        if (p == null) {
            return null;
        }
        while (p != null) {
            if (key < p.key) {
                if (p.getLeft() != null) {
                    p = p.getLeft();
                } else {
                    return p;
                }
            } else if (key > p.key) {
                if (p.getRight() != null) {
                    p = p.getRight();
                } else {
                    return p.next();
                }
            } else {
                return p.next();
            }
        }
        return null;
    }

    public LongTreeNode first() {
        LongTreeNode p = root;
        if (p == null) {
            return null;
        }
        while (p != null) {
            if (p.getLeft() != null) {
                p = p.getLeft();
            } else {
                return p;
            }
        }
        return null;
    }

    public LongTreeNode last() {
        LongTreeNode p = root;
        if (p == null) {
            return null;
        }
        while (p != null) {
            if (p.getRight() != null) {
                p = p.getRight();
            } else {
                return p;
            }
        }
        return null;
    }

    private void rotateLeft(LongTreeNode n) {
        LongTreeNode r = n.getRight();
        replaceNode(n, r);
        n.setRight(r.getLeft());
        if (r.getLeft() != null) {
            r.getLeft().setParent(n);
        }
        r.setLeft(n);
        n.setParent(r);
    }

    private void rotateRight(LongTreeNode n) {
        LongTreeNode l = n.getLeft();
        replaceNode(n, l);
        n.setLeft(l.getRight());
        if (l.getRight() != null) {
            l.getRight().setParent(n);
        }
        l.setRight(n);
        ;
        n.setParent(l);
    }

    private void replaceNode(LongTreeNode oldn, LongTreeNode newn) {
        if (oldn.getParent() == null) {
            root = newn;
        } else {
            if (oldn == oldn.getParent().getLeft()) {
                oldn.getParent().setLeft(newn);
            } else {
                oldn.getParent().setRight(newn);
            }
        }
        if (newn != null) {
            newn.setParent(oldn.getParent());
        }
    }

    public synchronized void insert(long key, long value) {
        resetCache();
        _dirty = true;
        LongTreeNode insertedNode = new LongTreeNode(key, value, false, null, null);
        if (root == null) {
            _size++;
            root = insertedNode;
        } else {
            LongTreeNode n = root;
            while (true) {
                if (key == n.key) {
                    n.value = value;
                    //nop _size
                    return;
                } else if (key < n.key) {
                    if (n.getLeft() == null) {
                        n.setLeft(insertedNode);
                        _size++;
                        break;
                    } else {
                        n = n.getLeft();
                    }
                } else {
                    if (n.getRight() == null) {
                        n.setRight(insertedNode);
                        _size++;
                        break;
                    } else {
                        n = n.getRight();
                    }
                }
            }
            insertedNode.setParent(n);
        }
        insertCase1(insertedNode);
    }

    private void insertCase1(LongTreeNode n) {
        if (n.getParent() == null) {
            n.color = true;
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(LongTreeNode n) {
        if (nodeColor(n.getParent()) == true) {
            return;
        } else {
            insertCase3(n);
        }
    }

    private void insertCase3(LongTreeNode n) {
        if (nodeColor(n.uncle()) == false) {
            n.getParent().color = true;
            n.uncle().color = true;
            n.grandparent().color = false;
            insertCase1(n.grandparent());
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(LongTreeNode n_n) {
        LongTreeNode n = n_n;
        if (n == n.getParent().getRight() && n.getParent() == n.grandparent().getLeft()) {
            rotateLeft(n.getParent());
            n = n.getLeft();
        } else {
            if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getRight()) {
                rotateRight(n.getParent());
                n = n.getRight();
            }
        }
        insertCase5(n);
    }

    private void insertCase5(LongTreeNode n) {
        n.getParent().color = true;
        n.grandparent().color = false;
        if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getLeft()) {
            rotateRight(n.grandparent());
        } else {
            rotateLeft(n.grandparent());
        }
    }

    public void delete(long key) {
        LongTreeNode n = internal_lookup(key);
        if (n == null) {
            return;
        } else {
            _size--;
            if (n.getLeft() != null && n.getRight() != null) {
                // Copy domainKey/value from predecessor and done delete it instead
                LongTreeNode pred = n.getLeft();
                while (pred.getRight() != null) {
                    pred = pred.getRight();
                }
                n.key = pred.key;
                n.value = pred.value;
                n = pred;
            }
            LongTreeNode child;
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

    private void deleteCase1(LongTreeNode n) {
        if (n.getParent() == null) {
            return;
        } else {
            deleteCase2(n);
        }
    }

    private void deleteCase2(LongTreeNode n) {
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

    private void deleteCase3(LongTreeNode n) {
        if (nodeColor(n.getParent()) == true && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == true && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            deleteCase1(n.getParent());
        } else {
            deleteCase4(n);
        }
    }

    private void deleteCase4(LongTreeNode n) {
        if (nodeColor(n.getParent()) == false && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == true && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            n.getParent().color = true;
        } else {
            deleteCase5(n);
        }
    }

    private void deleteCase5(LongTreeNode n) {
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

    private void deleteCase6(LongTreeNode n) {
        n.sibling().color = nodeColor(n.getParent());
        n.getParent().color = true;
        if (n == n.getParent().getLeft()) {
            n.sibling().getRight().color = true;
            rotateLeft(n.getParent());
        } else {
            n.sibling().getLeft().color = true;
            rotateRight(n.getParent());
        }
    }

    private boolean nodeColor(LongTreeNode n) {
        if (n == null) {
            return true;
        } else {
            return n.color;
        }
    }

}
