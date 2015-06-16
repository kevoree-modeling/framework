package org.kevoree.modeling.memory.struct.tree;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

public abstract class BaseKLongTreeTest {
    private static final int RANGE_TEST_SIZE = 100;

    public abstract KLongTree createLongTree();

    @Test
    public void printTest() {
        long MIN = 0L;
        long MAX = 99L;
        for (long j = MIN; j <= MAX; j++) {
            KLongTree tree = createLongTree();
            for (long i = MIN; i <= j; i++) {
                if ((i % 3) == 0L) {
                    tree.insert(i);
                } else {
                    tree.insert(i);
                }
            }
        }
    }


    /*
    @Test
    public void nextTest() {
        long MIN = 0L;
        long MAX = 99L;
        for (long j = MIN; j <= MAX; j++) {
            OOKLongTree tree = new OOKLongTree();
            for (long i = MIN; i <= j; i++) {
                if ((i % 3) == 0L) {
                    tree.insert(i);
                } else {
                    tree.insert(i);
                }
            }
            for (long i = MIN; i < j - 1; i++) {
                Assert.assertTrue(tree.next(i).getKey() == i + 1);
            }
            Assert.assertTrue(tree.next(j) == null);
        }
    }*/

    /*
    private void printTree(TreeNode root) {
        LinkedList<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(root);
        queue.add(null);
        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();
            while (current != null) {
                System.out.print("| " + current.getKey() + " ");
                if (current.getLeft() != null) {
                    queue.add(current.getLeft());
                }
                if (current.getRight() != null) {
                    queue.add(current.getRight());
                }
                current = queue.poll();
            }
            System.out.println();
            if (!queue.isEmpty()) {
                queue.add(null);
            }
        }
    }*/

    /*
    @Test
    public void previousTest() {
        long MIN = 0L;
        long MAX = 99L;
        for (long j = MIN + 1; j <= MAX; j++) {
            OOKLongTree tree = new OOKLongTree();
            for (long i = MIN; i <= j; i++) {
                if ((i % 7) == 0L) {
                    tree.insert(i);
                } else {
                    tree.insert(i);
                }
            }
            for (long i = j; i > MIN; i--) {
                Assert.assertTrue(tree.previous(i).getKey() == i - 1);
            }
            Assert.assertTrue(tree.previous(MIN) == null);
        }
    }*/

    /*
    @Test
    public void firstTest() {
        long MIN = 0L;
        long MAX = 99L;
        for (long j = MIN + 1; j <= MAX; j++) {
            OOKLongTree tree = new OOKLongTree();
            for (long i = MIN; i <= j; i++) {
                if ((i % 3) == 0L) {
                    tree.insert(i);
                } else {
                    tree.insert(i);
                }
            }
            Assert.assertTrue(tree.first().getKey() == MIN);
        }
    }*/

    /*
    @Test
    public void lastTest() {
        long MIN = 0L;
        long MAX = 99L;
        for (long j = MIN + 1; j <= MAX; j++) {
            OOKLongTree tree = new OOKLongTree();
            for (long i = MIN; i <= j; i++) {
                if ((i % 3) == 0L) {
                    tree.insert(i);
                } else {
                    tree.insert(i);
                }
            }
            Assert.assertTrue(tree.last().getKey() == j);
        }
    }*/

    @Test
    public void previousOrEqualTest() {
        KLongTree tree = createLongTree();
        for (long i = 0; i <= 6; i++) {
            tree.insert(i);
        }
        tree.insert(8L);
        tree.insert(10L);
        tree.insert(11L);
        tree.insert(13L);
        //printTree(tree.root);
        Assert.assertEquals(tree.previousOrEqual(-1), KConfig.NULL_LONG);
        Assert.assertEquals(tree.previousOrEqual(0), 0L);
        Assert.assertEquals(tree.previousOrEqual(1), 1L);
        Assert.assertEquals(tree.previousOrEqual(7), 6L);
        Assert.assertEquals(tree.previousOrEqual(8), 8L);
        Assert.assertEquals(tree.previousOrEqual(9), 8L);
        Assert.assertEquals(tree.previousOrEqual(10), 10L);
        Assert.assertEquals(tree.previousOrEqual(13), 13L);
        Assert.assertEquals(tree.previousOrEqual(14), 13L);
    }

    /*
    @Test
    public void nextOrEqualTest() {
        OOKLongTree tree = new OOKLongTree();
        for (long i = 0; i <= 6; i++) {
            tree.insert(i);
        }
        tree.insert(8L);
        tree.insert(10L);
        tree.insert(11L);
        tree.insert(13L);
        //printTree(tree.root!!)
        Assert.assertTrue(tree.nextOrEqual(-1).getKey() == 0L);
        Assert.assertTrue(tree.nextOrEqual(0).getKey() == 0L);
        Assert.assertTrue(tree.nextOrEqual(1).getKey() == 1L);
        Assert.assertTrue(tree.nextOrEqual(7).getKey() == 8L);
        Assert.assertTrue(tree.nextOrEqual(8).getKey() == 8L);
        Assert.assertTrue(tree.nextOrEqual(9).getKey() == 10L);
        Assert.assertTrue(tree.nextOrEqual(10).getKey() == 10L);
        Assert.assertTrue(tree.nextOrEqual(13).getKey() == 13L);
        Assert.assertNull(tree.nextOrEqual(14));
    }*/

    @Test
    public void cacheEffectTest() {
        KLongTree tree = createLongTree();
        for (long i = 0; i <= 6; i++) {
            tree.insert(i);
        }
        Assert.assertTrue(tree.previousOrEqual(-1) == KConfig.NULL_LONG);
        Assert.assertTrue(tree.previousOrEqual(0) == 0l);
        Assert.assertTrue(tree.previousOrEqual(1) == 1l);
        Assert.assertTrue(tree.previousOrEqual(0) == 0l);

        tree.insert(7);
        Assert.assertTrue(tree.previousOrEqual(7) == 7l);
        Assert.assertTrue(tree.previousOrEqual(7) == 7l);
        Assert.assertTrue(tree.previousOrEqual(8) == 7l);
        Assert.assertTrue(tree.previousOrEqual(9) == 7l);
        //Assert.assertTrue(tree.previousOrEqual(7).getKey() == 7l);
        Assert.assertTrue(tree.previousOrEqual(10) == 7l);
        Assert.assertTrue(tree.previousOrEqual(7) == 7l);

    }

    @Test
    public void rangeTest() {
        KLongTree tree = createLongTree();

        for (int i = 0; i < RANGE_TEST_SIZE; i++) {
            tree.insert(i);
        }

        final MutableInteger integer = new MutableInteger();

        //in the full range
        integer.set(0);
        tree.range(0, RANGE_TEST_SIZE, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(RANGE_TEST_SIZE, integer.get());

        //in the beginning
        integer.set(0);
        tree.range(0, 20, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(21, integer.get());

        //in the middle, single
        integer.set(0);
        tree.range(20, 20, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(1, integer.get());

        //in the middle
        integer.set(0);
        tree.range(20, 79, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(60, integer.get());

        //slightly above the limit
        integer.set(0);
        tree.range(RANGE_TEST_SIZE - 20, RANGE_TEST_SIZE, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(20, integer.get());

        //above the limit
        integer.set(0);
        tree.range(RANGE_TEST_SIZE - 20, RANGE_TEST_SIZE + 50, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(20, integer.get());

        //before the limit left
        integer.set(0);
        tree.range(-50, 99, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(RANGE_TEST_SIZE, integer.get());

        //before the limit left and right
        integer.set(0);
        tree.range(-50, 150, new KTreeWalker() {
            @Override
            public void elem(long t) {
                integer.increment();
            }
        });
        Assert.assertEquals(RANGE_TEST_SIZE, integer.get());
    }
}
