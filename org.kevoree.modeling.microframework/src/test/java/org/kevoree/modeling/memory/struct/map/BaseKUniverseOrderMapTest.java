package org.kevoree.modeling.memory.struct.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

public abstract class BaseKUniverseOrderMapTest {

    private static final int SIZE = 100;

    public abstract KUniverseOrderMap createKUniverseOrderMap(int p_initalCapacity, float p_loadFactor, String p_className);

    @Test
    public void test() {
        KLongLongMap map = createKUniverseOrderMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR, "org.kevoree.modeling.Hello");
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
        KUniverseOrderMap map = createKUniverseOrderMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR, "org.kevoree.modeling.Hello");
        for (long i = 0; i < 10; i++) {
            map.put(i, i);
        }
        Assert.assertEquals(map.size(), 10);
        String saved = map.serialize(null);

        Assert.assertEquals(saved, "org.kevoree.modeling.Hello,10{\"0\":0,\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7,\"8\":8,\"9\":9}");

        KUniverseOrderMap map2 = createKUniverseOrderMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR, null);
        map2.init(saved, null);
        Assert.assertEquals(map2.size(), 10);
        for (long i = 0; i < 10; i++) {
            Assert.assertEquals(map.get(i), i);
            Assert.assertEquals(map2.get(i), i);
        }

        Assert.assertEquals(map.size(), map2.size());
        Assert.assertEquals(map.metaClassName(), map2.metaClassName());

        String saved2 = map2.serialize(null);

        Assert.assertEquals(saved2, "org.kevoree.modeling.Hello,10{\"0\":0,\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7,\"8\":8,\"9\":9}");

        KUniverseOrderMap nullClassName = createKUniverseOrderMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR, null);
        for (long i = 0; i < 10; i++) {
            nullClassName.put(i, i);
        }
        String nullSaved = nullClassName.serialize(null);
        Assert.assertEquals(nullSaved, "10{\"0\":0,\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7,\"8\":8,\"9\":9}");
        KUniverseOrderMap mapNull2 = createKUniverseOrderMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR, null);
        mapNull2.init(nullSaved, null);
        String nullSaved2 = mapNull2.serialize(null);
        Assert.assertEquals(nullSaved2, "10{\"0\":0,\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7,\"8\":8,\"9\":9}");

    }

}
