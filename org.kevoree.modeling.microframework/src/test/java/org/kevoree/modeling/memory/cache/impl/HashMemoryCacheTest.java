package org.kevoree.modeling.memory.cache.impl;

import org.kevoree.modeling.memory.cache.BaseKCacheTest;
import org.kevoree.modeling.memory.cache.KCache;

/**
 * Created by duke on 20/02/15.
 */
public class HashMemoryCacheTest extends BaseKCacheTest {

    @Override
    public KCache createKCache() {
        return new HashMemoryCache();
    }
}
