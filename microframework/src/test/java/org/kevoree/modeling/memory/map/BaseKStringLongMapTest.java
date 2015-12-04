package org.kevoree.modeling.memory.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.memory.chunk.KStringLongMap;
import org.kevoree.modeling.memory.chunk.KStringLongMapCallBack;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.impl.MetaModel;

public abstract class BaseKStringLongMapTest {

    private static final int SIZE = 100;

    public abstract KStringLongMap createKStringLongMap();

    /*
    @Test
    public void test() {

        KStringLongMap map = createKStringLongMap();
        KMetaModel metaModel = new MetaModel("UniverseOrderMapTest");
       // map.init(null, metaModel, -1);
        for (long i = 0; i < SIZE; i++) {
            map.put("k_" + i, i);
        }
        Assert.assertEquals(map.size(), SIZE);
        for (long i = 0; i < SIZE; i++) {
            Assert.assertEquals(i, map.get("k_" + i));
        }
        final int[] nbCall = {0};
        map.each(new KStringLongMapCallBack() {
            @Override
            public void on(String key, long s) {
                nbCall[0]++;
                Assert.assertEquals(key, "k_" + s);
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
        KStringLongMap map = createKStringLongMap();
        map.init(null, metaModel, metaClass.index());
        for (long i = 0; i < 10; i++) {
            map.put("k_" + i, i);
        }
        Assert.assertEquals(map.size(), 10);
        String saved = map.serialize(metaModel);
        Assert.assertEquals("U/a18w:A,a18x:C,a18y:E,a18z:G,a180:I,a181:K,a182:M,a183:O,a184:Q,a185:S", saved);
        map.init(saved, metaModel, metaClass.index()); //init again and simulate a reload
        saved = map.serialize(metaModel);
        Assert.assertEquals("U/a18w:A,a18x:C,a18y:E,a18z:G,a180:I,a181:K,a182:M,a183:O,a184:Q,a185:S", saved);

        KStringLongMap map2 = createKStringLongMap();//, null);
        map2.init(saved, metaModel, -1);
        Assert.assertEquals(map2.size(), 10);
        for (long i = 0; i < 10; i++) {
            Assert.assertEquals(map.get("k_" + i), i);
            Assert.assertEquals(map2.get("k_" + i), i);
        }
        Assert.assertEquals(map.size(), map2.size());
        String saved2 = map2.serialize(metaModel);
        Assert.assertEquals("U/a18w:A,a18x:C,a18y:E,a18z:G,a180:I,a181:K,a182:M,a183:O,a184:Q,a185:S", saved2);
        KStringLongMap nullClassName = createKStringLongMap();//, null);
        for (long i = 0; i < 10; i++) {
            nullClassName.put("k_" + i, i);
        }
        String nullSaved = nullClassName.serialize(metaModel);
        Assert.assertEquals("U/a18w:A,a18x:C,a18y:E,a18z:G,a180:I,a181:K,a182:M,a183:O,a184:Q,a185:S", nullSaved);
        KStringLongMap mapNull2 = createKStringLongMap();//, null);
        mapNull2.init(nullSaved, metaModel, -1);
        String nullSaved2 = mapNull2.serialize(metaModel);
        Assert.assertEquals("U/a18w:A,a18x:C,a18y:E,a18z:G,a180:I,a181:K,a182:M,a183:O,a184:Q,a185:S", nullSaved2);
    }

    @Test
    public void testInitThenPut() {
        KStringLongMap map = createKStringLongMap();//, "org.kevoree.modeling.Hello");
        map.init(null, null, -1);
        map.put("k_" + 0, 0);
        map.put("k_" + 1, 1);
    }
*/
}
