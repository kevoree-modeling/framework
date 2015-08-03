package org.kevoree.modeling.memory.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;

public abstract class BaseKLongLongMapTest {

    private static final int SIZE = 100;

    public abstract KLongLongMap createKLongLongMap();

    @Test
    public void test() {
        KLongLongMap map = createKLongLongMap();
        KMetaModel metaModel = new MetaModel("UniverseOrderMapTest");
        map.init(null, metaModel, -1);
        for (long i = 0; i < SIZE; i++) {
            map.put(i, i);
        }
        Assert.assertEquals(map.size(), SIZE);
        for (long i = 0; i < SIZE; i++) {
            Assert.assertEquals(i, map.get(i));
        }
        final int[] nbCall = {0};
        map.each(new KLongLongMapCallBack() {
            @Override
            public void on(long key, long s) {
                nbCall[0]++;
                Assert.assertEquals(key, s);
            }
        });
        Assert.assertEquals(nbCall[0], SIZE);
        map.clear();
        Assert.assertEquals(map.size(), 0);
    }

    @Test
    public void testLoadSave() throws Exception {
        KMetaModel metaModel = new MetaModel("UniverseOrderMapTest");
        KMetaClass metaClass = metaModel.addMetaClass("org.kevoree.modeling.Hello");
        KLongLongMap map = createKLongLongMap();
        map.init(null, metaModel, metaClass.index());
        for (long i = 0; i < 10; i++) {
            map.put(i, i);
        }
        Assert.assertEquals(map.size(), 10);
        String saved = map.serialize(metaModel);
        Assert.assertEquals("org.kevoree.modeling.Hello,U/A:A,C:C,E:E,G:G,I:I,K:K,M:M,O:O,Q:Q,S:S", saved);
        map.init(saved, metaModel, metaClass.index()); //init again and simulate a reload
        saved = map.serialize(metaModel);
        Assert.assertEquals("org.kevoree.modeling.Hello,U/A:A,C:C,E:E,G:G,I:I,K:K,M:M,O:O,Q:Q,S:S", saved);

        KLongLongMap map2 = createKLongLongMap();//, null);
        map2.init(saved, metaModel, -1);
        Assert.assertEquals(map2.size(), 10);
        for (long i = 0; i < 10; i++) {
            Assert.assertEquals(map.get(i), i);
            Assert.assertEquals(map2.get(i), i);
        }
        Assert.assertEquals(map.size(), map2.size());
        Assert.assertEquals(map.metaClassIndex(), map2.metaClassIndex());
        String saved2 = map2.serialize(metaModel);
        Assert.assertEquals("org.kevoree.modeling.Hello,U/A:A,C:C,E:E,G:G,I:I,K:K,M:M,O:O,Q:Q,S:S", saved2);
        KLongLongMap nullClassName = createKLongLongMap();//, null);
        for (long i = 0; i < 10; i++) {
            nullClassName.put(i, i);
        }
        String nullSaved = nullClassName.serialize(metaModel);
        Assert.assertEquals("U/A:A,C:C,E:E,G:G,I:I,K:K,M:M,O:O,Q:Q,S:S", nullSaved);
        KLongLongMap mapNull2 = createKLongLongMap();//, null);
        mapNull2.init(nullSaved, metaModel, -1);
        String nullSaved2 = mapNull2.serialize(metaModel);
        Assert.assertEquals("U/A:A,C:C,E:E,G:G,I:I,K:K,M:M,O:O,Q:Q,S:S", nullSaved2);
    }

    @Test
    public void testInitThenPut() {
        KLongLongMap map = createKLongLongMap();//, "org.kevoree.modeling.Hello");
        map.init(null, null, -1);
        map.put(0, 0);
        map.put(1, 1);
    }

}
