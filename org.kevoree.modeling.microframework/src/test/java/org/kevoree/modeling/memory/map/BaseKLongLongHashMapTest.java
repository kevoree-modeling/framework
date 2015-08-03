package org.kevoree.modeling.memory.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

public abstract class BaseKLongLongHashMapTest {
    private static final int SIZE = 20;

    public abstract KLongLongMap createKLongLongMap(int p_initalCapacity, float p_loadFactor);

    @Test
    public void test() {

        KLongLongMap map = createKLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
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
        Assert.assertEquals(SIZE, nbCall[0]);
        map.clear();
        Assert.assertEquals(0, map.size());


        for (long i = 0; i < SIZE; i++) {
            map.put(i, i);
        }
        Assert.assertEquals(SIZE, map.size());
        map.remove(5);
        Assert.assertEquals(SIZE - 1, map.size());
        for (long i = 0; i < SIZE; i++) {
            if (i != 5) {
                Assert.assertEquals(i, map.get(i));
            } else {
                Assert.assertEquals(KConfig.NULL_LONG, map.get(i));
            }
        }
        map.put(SIZE, SIZE);
        Assert.assertEquals(SIZE, map.size());
        for (long i = 0; i <= SIZE; i++) {
            if (i != 5) {
                Assert.assertEquals(i, map.get(i));
            } else {
                Assert.assertEquals(KConfig.NULL_LONG, map.get(i));
            }
        }


    }
}
