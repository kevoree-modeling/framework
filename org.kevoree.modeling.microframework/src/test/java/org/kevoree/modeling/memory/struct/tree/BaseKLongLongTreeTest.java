package org.kevoree.modeling.memory.struct.tree;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

public abstract class BaseKLongLongTreeTest {

    public abstract KLongLongTree createKLongLongTree();

    @Test
    public void saveLoad0() throws Exception {
        KLongLongTree tree = createKLongLongTree();
        tree.init(null, null);

        for (long i = 0; i <= 6; i++) {
            tree.insert(i, i);
        }
        KLongLongTree treeBis = createKLongLongTree();
        treeBis.init(tree.serialize(null), null);
        Assert.assertEquals(tree.size(), treeBis.size());
    }

    @Test
    public void saveLoad() throws Exception {
        KLongLongTree tree = createKLongLongTree();
        tree.init(null, null);

        for (long i = 0; i <= 6; i++) {
            tree.insert(i, i);
        }
        KLongLongTree treeBis = createKLongLongTree();
        treeBis.init(tree.serialize(null), null);
        Assert.assertEquals(tree.size(), treeBis.size());
        for (int i = 0; i < tree.size(); i++) {
            Long resolved = tree.lookupValue(i);
            Long resolvedBis = treeBis.lookupValue(i);
            Assert.assertEquals(resolved, resolvedBis);
        }
    }

    @Test
    public void cacheEffectTest() {
        KLongLongTree tree = createKLongLongTree();
        tree.init(null, null);

        for (long i = 0; i <= 6; i++) {
            tree.insert(i, i);
        }
        Assert.assertTrue(tree.previousOrEqualValue(-1) == KConfig.NULL_LONG);
        Assert.assertTrue(tree.previousOrEqualValue(0) == 0l);
        Assert.assertTrue(tree.previousOrEqualValue(1) == 1l);
        Assert.assertTrue(tree.previousOrEqualValue(0) == 0l);

        tree.insert(7, 7);
        Assert.assertTrue(tree.previousOrEqualValue(7) == 7l);
        Assert.assertTrue(tree.previousOrEqualValue(7) == 7l);
        Assert.assertTrue(tree.previousOrEqualValue(8) == 7l);
        Assert.assertTrue(tree.previousOrEqualValue(9) == 7l);
        //Assert.assertTrue(tree.previousOrEqual(7).key == 7l);
        Assert.assertTrue(tree.previousOrEqualValue(10) == 7l);
        Assert.assertTrue(tree.previousOrEqualValue(7) == 7l);

    }
}
