package org.kevoree.modeling.memory.struct.tree.impl;

class LongTreeNode {

    public static final char BLACK = '0';
    public static final char RED = '2';

    public long key;

    public long value;

    public boolean color;

    private LongTreeNode left;

    private LongTreeNode right;

    private LongTreeNode parent = null;

    public LongTreeNode(long key, long value, boolean color, LongTreeNode left, LongTreeNode right) {
        this.key = key;
        this.value = value;
        this.color = color;
        this.left = left;
        this.right = right;
        if (left != null) {
            left.parent = this;
        }
        if (right != null) {
            right.parent = this;
        }
        this.parent = null;
    }

    public LongTreeNode grandparent() {
        if (parent != null) {
            return parent.parent;
        } else {
            return null;
        }
    }

    public LongTreeNode sibling() {
        if (parent == null) {
            return null;
        } else {
            if (this == parent.left) {
                return parent.right;
            } else {
                return parent.left;
            }
        }
    }

    public LongTreeNode uncle() {
        if (parent != null) {
            return parent.sibling();
        } else {
            return null;
        }
    }

    public LongTreeNode getLeft() {
        return left;
    }

    public void setLeft(LongTreeNode left) {
        this.left = left;
    }

    public LongTreeNode getRight() {
        return right;
    }

    public void setRight(LongTreeNode right) {
        this.right = right;
    }

    public LongTreeNode getParent() {
        return parent;
    }

    public void setParent(LongTreeNode parent) {
        this.parent = parent;
    }

    public void serialize(StringBuilder builder) {
        builder.append("|");
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
    }

    public LongTreeNode next() {
        LongTreeNode p = this;
        if (p.right != null) {
            p = p.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        } else {
            if (p.parent != null) {
                if (p == p.parent.left) {
                    return p.parent;
                } else {
                    while (p.parent != null && p == p.parent.right) {
                        p = p.parent;
                    }
                    return p.parent;
                }
            } else {
                return null;
            }
        }
    }

    public LongTreeNode previous() {
        LongTreeNode p = this;
        if (p.left != null) {
            p = p.left;
            while (p.right != null) {
                p = p.right;
            }
            return p;
        } else {
            if (p.parent != null) {
                if (p == p.parent.right) {
                    return p.parent;
                } else {
                    while (p.parent != null && p == p.parent.left) {
                        p = p.parent;
                    }
                    return p.parent;
                }
            } else {
                return null;
            }
        }
    }

    public static LongTreeNode unserialize(TreeReaderContext ctx) throws Exception {
        return internal_unserialize(true, ctx);
    }

    public static LongTreeNode internal_unserialize(boolean rightBranch, TreeReaderContext ctx) throws Exception {
        if (ctx.index >= ctx.payload.length()) {
            return null;
        }
        char ch = ctx.payload.charAt(ctx.index);
        if (ch == '%') {
            if (rightBranch) {
                ctx.index = ctx.index + 1;
            }
            return null;
        }
        if (ch == '#') {
            ctx.index = ctx.index + 1;
            return null;
        }
        if (ch != '|') {
            throw new Exception("Error while loading BTree");
        }
        ctx.index = ctx.index + 1;
        ch = ctx.payload.charAt(ctx.index);
        boolean colorLoaded = true;
        if (ch == RED) {
            colorLoaded = false;
        }
        ctx.index = ctx.index + 1;
        ch = ctx.payload.charAt(ctx.index);
        int i = 0;
        while (ctx.index + 1 < ctx.payload.length() && ch != '|' && ch != '#' && ch != '%' && ch != '@') {
            ctx.buffer[i] = ch;
            i++;
            ctx.index = ctx.index + 1;
            ch = ctx.payload.charAt(ctx.index);
        }
        if (ch != '|' && ch != '#' && ch != '%' && ch != '@') {
            ctx.buffer[i] = ch;
            i++;
        }
        Long key = Long.parseLong(String.copyValueOf(ctx.buffer, 0, i));
        i=0;
        ctx.index = ctx.index + 1; //We drop separator
        ch = ctx.payload.charAt(ctx.index);
        while (ctx.index + 1 < ctx.payload.length() && ch != '|' && ch != '#' && ch != '%' && ch != '@') {
            ctx.buffer[i] = ch;
            i++;
            ctx.index = ctx.index + 1;
            ch = ctx.payload.charAt(ctx.index);
        }
        if (ch != '|' && ch != '#' && ch != '%' && ch != '@') {
            ctx.buffer[i] = ch;
            i++;
        }
        Long value = Long.parseLong(String.copyValueOf(ctx.buffer, 0, i));
        LongTreeNode p = new LongTreeNode(key, value, colorLoaded, null, null);
        LongTreeNode left = internal_unserialize(false, ctx);
        if (left != null) {
            left.setParent(p);
        }
        LongTreeNode right = internal_unserialize(true, ctx);
        if (right != null) {
            right.setParent(p);
        }
        p.setLeft(left);
        p.setRight(right);
        return p;
    }


}
