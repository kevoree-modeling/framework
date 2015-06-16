package org.kevoree.modeling.memory.struct.tree.impl;

class TreeNode {

    public static final char BLACK = '0';
    public static final char RED = '1';

    protected long key;

    protected boolean color;

    private TreeNode left;

    private TreeNode right;

    private TreeNode parent = null;

    public TreeNode(long key, boolean color, TreeNode left, TreeNode right) {
        this.key = key;
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

    public long getKey() {
        return key;
    }

    public TreeNode grandparent() {
        if (parent != null) {
            return parent.parent;
        } else {
            return null;
        }
    }

    public TreeNode sibling() {
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

    public TreeNode uncle() {
        if (parent != null) {
            return parent.sibling();
        } else {
            return null;
        }
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
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

    public TreeNode next() {
        TreeNode p = this;
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

    public TreeNode previous() {
        TreeNode p = this;
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

    public static TreeNode unserialize(TreeReaderContext ctx) throws Exception {
        return internal_unserialize(true, ctx);
    }

    public static TreeNode internal_unserialize(boolean rightBranch, TreeReaderContext ctx) throws Exception {
        if (ctx.index >= ctx.payload.length()) {
            return null;
        }
        StringBuilder tokenBuild = new StringBuilder();
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
        boolean colorLoaded;
        if (ch == TreeNode.BLACK) {
            colorLoaded = true;
        } else {
            colorLoaded = false;
        }
        ctx.index = ctx.index + 1;
        ch = ctx.payload.charAt(ctx.index);
        while (ctx.index + 1 < ctx.payload.length() && ch != '|' && ch != '#' && ch != '%') {
            tokenBuild.append(ch);
            ctx.index = ctx.index + 1;
            ch = ctx.payload.charAt(ctx.index);
        }
        if (ch != '|' && ch != '#' && ch != '%') {
            tokenBuild.append(ch);
        }
        TreeNode p = new TreeNode(Long.parseLong(tokenBuild.toString()), colorLoaded, null, null);
        TreeNode left = internal_unserialize(false, ctx);
        if (left != null) {
            left.setParent(p);
        }
        TreeNode right = internal_unserialize(true, ctx);
        if (right != null) {
            right.setParent(p);
        }
        p.setLeft(left);
        p.setRight(right);
        return p;
    }

}
