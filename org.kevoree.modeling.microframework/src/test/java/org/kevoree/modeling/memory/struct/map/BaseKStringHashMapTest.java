package org.kevoree.modeling.memory.struct.map;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;

import java.util.HashMap;

public abstract class BaseKStringHashMapTest {

    public abstract KStringMap createKStringHashMap(int p_initalCapacity, float p_loadFactor);

    @Test
    public void test() {
        RandomString randomString = new RandomString(10);
        HashMap<String, String> origin = new HashMap<String, String>();
        KStringMap<String> optimized = createKStringHashMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

        int nbLoop = 100;
        for (int i = 0; i < nbLoop; i++) {
            String newVal = randomString.nextString();
            origin.put(newVal, newVal);
            optimized.put(newVal, newVal);
        }
        Assert.assertEquals(nbLoop, origin.keySet().size());
        Assert.assertEquals(nbLoop, optimized.size());
        final int[] loopElem = {0};
        optimized.each(new KStringMapCallBack<String>() {
            @Override
            public void on(String key, String value) {
                Assert.assertEquals(key, value);
                String originVal = origin.get(key);
                Assert.assertEquals(key, originVal);
                loopElem[0]++;
            }
        });
        Assert.assertEquals(nbLoop, loopElem[0]);
    }

    @Test
    public void emptyTest() {
        KStringMap<String> optimized = createKStringHashMap(0, KConfig.CACHE_LOAD_FACTOR);
        Assert.assertEquals(optimized.size(), 0);
        Assert.assertTrue(!optimized.contains("randomKey"));
        Assert.assertNull(optimized.get("randomKey"));
        optimized.put("randomKey", "randomVal");
        Assert.assertTrue(optimized.contains("randomKey"));
        Assert.assertNotNull(optimized.get("randomKey"));
    }
}
