package org.kevoree.modeling.memory.cache.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KContentKey;

/**
 * Created by duke on 20/02/15.
 */
public class KContentKeyTest {

    @Test
    public void test() {
        String[] testKeys = new String[5];
        testKeys[0] = "1//";
        testKeys[1] = "//4";
        testKeys[2] = "//4";
        testKeys[3] = "2/3/4";
        testKeys[4] = "222/333/444";
        for (int i = 0; i < testKeys.length; i++) {
            Assert.assertEquals(testKeys[i], KContentKey.create(testKeys[i]).toString());
        }
    }

}
