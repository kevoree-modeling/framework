package org.kevoree.modeling.memory.struct.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

public abstract class BaseKLongLongHashMapTest {
    private static final int SIZE = 100;

    public abstract KLongLongMap createKLongLongHashMap(int p_initalCapacity, float p_loadFactor);

    @Test
    public void test() {

        KLongLongMap map = createKLongLongHashMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
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
}
