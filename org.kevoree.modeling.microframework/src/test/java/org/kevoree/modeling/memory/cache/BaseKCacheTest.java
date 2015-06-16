package org.kevoree.modeling.memory.cache;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.meta.KMetaModel;

public abstract class BaseKCacheTest {

    public abstract KCache createKCache();

    @Test
    public void test() {
        KCache cache = createKCache();
        KMemoryElement temp = new KMemoryElement() {
            @Override
            public boolean isDirty() {
                return false;
            }

            @Override
            public String serialize(KMetaModel metaModel) {
                return null;
            }

            @Override
            public void setClean(KMetaModel mm) {
            }

            @Override
            public void setDirty() {

            }

            @Override
            public void init(String payload, KMetaModel metaModel) throws Exception {
            }

            @Override
            public int counter() {
                return 0;
            }

            @Override
            public void inc() {
            }

            @Override
            public void dec() {
            }

            @Override
            public void free(KMetaModel metaModel) {

            }
        };
        cache.put(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG, temp);
        Assert.assertEquals(temp, cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG));
        cache.put(0, KConfig.NULL_LONG, KConfig.NULL_LONG, temp);
        Assert.assertEquals(temp, cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG));
        Assert.assertEquals(temp, cache.get(0, KConfig.NULL_LONG, KConfig.NULL_LONG));
        cache.put(KConfig.NULL_LONG, KConfig.NULL_LONG, 2, temp);
        Assert.assertEquals(temp, cache.get(KConfig.NULL_LONG, KConfig.NULL_LONG, 2));
    }
}

