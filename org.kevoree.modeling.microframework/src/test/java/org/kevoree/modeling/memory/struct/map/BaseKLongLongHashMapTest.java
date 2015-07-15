package org.kevoree.modeling.memory.struct.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.impl.OffHeapLongLongMap;

public abstract class BaseKLongLongHashMapTest {
    private static final int SIZE = 13;

    public abstract KLongLongMap createKLongLongHashMap(int p_initalCapacity, float p_loadFactor);

    @Test
    public void test() {

        KLongLongMap map = createKLongLongHashMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        for (long i = 0; i < SIZE; i++) {
            map.put(i, i);
        }

        System.out.println(map.get(2));

//        Assert.assertEquals(map.size(), SIZE);
//        for (long i = 0; i < SIZE; i++) {
//            Assert.assertEquals(i, map.get(i));
//        }

//        final int[] nbCall = {0};
//        map.each(new KLongLongMapCallBack() {
//            @Override
//            public void on(long key, long s) {
//                nbCall[0]++;
//                Assert.assertEquals(key, s);
//            }
//        });
//        Assert.assertEquals(SIZE, nbCall[0]);
//        map.clear();
//        Assert.assertEquals(0, map.size());
    }
}
