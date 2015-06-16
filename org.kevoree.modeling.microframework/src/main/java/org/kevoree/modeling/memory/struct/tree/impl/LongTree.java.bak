package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;

public class LongTree implements KMemoryElement, KLongTree {

    private int _size = 0;
    private TreeNode root = null;
    private TreeNode[] _previousOrEqualsCacheValues = null;
    private int _nextCacheElem;
    private int _counter = 0;
    private boolean _dirty = false;

    public LongTree() {
        _previousOrEqualsCacheValues = new TreeNode[KConfig.TREE_CACHE_SIZE];
        _nextCacheElem = 0;
    }

    public int size() {
        return _size;
    }

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

    /* Cache management */
    private TreeNode tryPreviousOrEqualsCache(long key) {
        if (_previousOrEqualsCacheValues != null) {
            for (int i = 0; i < _nextCacheElem; i++) {
                if (_previousOrEqualsCacheValues[i] != null && _previousOrEqualsCacheValues[i].key == key) {
                    return _previousOrEqualsCacheValues[i];
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private void resetCache() {
        _nextCacheElem = 0;
    }

    private void putInPreviousOrEqualsCache(TreeNode resolved) {
        if (_nextCacheElem == KConfig.TREE_CACHE_SIZE) {
            _nextCacheElem = 0;
        }
        _previousOrEqualsCacheValues[_nextCacheElem] = resolved;
        _nextCacheElem++;
    }

    @Override
    public boolean isDirty() {
        return this._dirty;
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        this._dirty = false;
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

    @Override
    public String toString() {
        return serialize(null);
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
        root = TreeNode.unserialize(ctx);
        resetCache();
    }

    public synchronized long previousOrEqual(long key) {
        TreeNode resolvedNode = internal_previousOrEqual(key);
        if (resolvedNode != null) {
            return resolvedNode.key;
        }
        return KConfig.NULL_LONG;
    }

    public synchronized TreeNode internal_previousOrEqual(long key) {
        TreeNode cachedVal = tryPreviousOrEqualsCache(key);
        if (cachedVal != null) {
            return cachedVal;
        }
        TreeNode p = root;
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
                    TreeNode parent = p.getParent();
                    TreeNode ch = p;
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

    public TreeNode nextOrEqual(long key) {
        TreeNode p = root;
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
                    TreeNode parent = p.getParent();
                    TreeNode ch = p;
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

    public TreeNode previous(long key) {
        TreeNode p = root;
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

    public TreeNode next(long key) {
        TreeNode p = root;
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

    public TreeNode first() {
        TreeNode p = root;
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

    public TreeNode last() {
        TreeNode p = root;
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

    /* Time never use direct lookup, sadly for performance, anyway this method is private to ensure the correctness of caching mechanism */
    @Override
    public long lookup(long key) {
        TreeNode n = root;
        if (n == null) {
            return KConfig.NULL_LONG;
        }
        while (n != null) {
            if (key == n.key) {
                return n.key;
            } else {
                if (key < n.key) {
                    n = n.getLeft();
                } else {
                    n = n.getRight();
                }
            }
        }
        return KConfig.NULL_LONG;
    }

    @Override
    public void range(long start, long end, KTreeWalker walker) {
        TreeNode it = internal_previousOrEqual(end);
        while (it != null && it.key >= start) {
            walker.elem(it.key);
            it = it.previous();
        }
    }

    private void rotateLeft(TreeNode n) {
        TreeNode r = n.getRight();
        replaceNode(n, r);
        n.setRight(r.getLeft());
        if (r.getLeft() != null) {
            r.getLeft().setParent(n);
        }
        r.setLeft(n);
        n.setParent(r);
    }

    private void rotateRight(TreeNode n) {
        TreeNode l = n.getLeft();
        replaceNode(n, l);
        n.setLeft(l.getRight());
        if (l.getRight() != null) {
            l.getRight().setParent(n);
        }
        l.setRight(n);
        ;
        n.setParent(l);
    }

    private void replaceNode(TreeNode oldn, TreeNode newn) {
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

    public synchronized void insert(long key) {
        this._dirty = true;
        TreeNode insertedNode;
        if (root == null) {
            _size++;
            insertedNode = new TreeNode(key, false, null, null);
            root = insertedNode;
        } else {
            TreeNode n = root;
            while (true) {
                if (key == n.key) {
                    putInPreviousOrEqualsCache(n);
                    //nop _size
                    return;
                } else if (key < n.key) {
                    if (n.getLeft() == null) {
                        insertedNode = new TreeNode(key, false, null, null);
                        n.setLeft(insertedNode);
                        _size++;
                        break;
                    } else {
                        n = n.getLeft();
                    }
                } else {
                    if (n.getRight() == null) {
                        insertedNode = new TreeNode(key, false, null, null);
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
        putInPreviousOrEqualsCache(insertedNode);
    }

    private void insertCase1(TreeNode n) {
        if (n.getParent() == null) {
            n.color = true;
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(TreeNode n) {
        if (nodeColor(n.getParent()) == true) {
            return;
        } else {
            insertCase3(n);
        }
    }

    private void insertCase3(TreeNode n) {
        if (nodeColor(n.uncle()) == false) {
            n.getParent().color = true;
            n.uncle().color = true;
            n.grandparent().color = false;
            insertCase1(n.grandparent());
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(TreeNode n_n) {
        TreeNode n = n_n;
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

    private void insertCase5(TreeNode n) {
        n.getParent().color = true;
        n.grandparent().color = false;
        if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getLeft()) {
            rotateRight(n.grandparent());
        } else {
            rotateLeft(n.grandparent());
        }
    }

    public void delete(long key) {
        TreeNode n = null;
        TreeNode nn = root;
        while (nn != null) {
            if (key == nn.key) {
                n = nn;
            } else {
                if (key < nn.key) {
                    nn = nn.getLeft();
                } else {
                    nn = nn.getRight();
                }
            }
        }
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
    }

    private boolean nodeColor(TreeNode n) {
        if (n == null) {
            return true;
        } else {
            return n.color;
        }
    }

}
