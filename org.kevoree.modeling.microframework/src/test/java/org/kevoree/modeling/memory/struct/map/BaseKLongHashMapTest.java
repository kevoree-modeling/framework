package org.kevoree.modeling.memory.struct.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

public abstract class BaseKLongHashMapTest {
    private static final int SIZE = 100;

    public abstract KLongMap createKLongHashMap(int p_initalCapacity, float p_loadFactor);

    @Test
    public void test() {

        KLongMap<String> map = createKLongHashMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        for (long i = 0; i < SIZE; i++) {
            map.put(i, "" + i);
        }
        Assert.assertEquals(map.size(), SIZE);
        for (long i = 0; i < SIZE; i++) {
            Assert.assertEquals(i, Long.parseLong(map.get(i)));
        }
        final int[] nbCall = {0};
        map.each(new KLongMapCallBack<String>() {
            @Override
            public void on(long key, String s) {
                nbCall[0]++;
                Assert.assertEquals(key, Long.parseLong(s));
            }
        });
        Assert.assertEquals(nbCall[0], SIZE);
        map.clear();
        Assert.assertEquals(map.size(), 0);
    }
}
