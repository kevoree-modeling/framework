package org.kevoree.modeling.memory.struct.cache.impl;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;

/**
 * Created by duke on 20/02/15.
 */
public class KContentKeyTest {

    @Test
    public void test() {
        String[] testKeys = new String[5];
        testKeys[0] = "1"+ KConfig.KEY_SEP+""+ KConfig.KEY_SEP+"";
        testKeys[1] = ""+ KConfig.KEY_SEP+""+ KConfig.KEY_SEP+"4";
        testKeys[2] = ""+ KConfig.KEY_SEP+""+ KConfig.KEY_SEP+"4";
        testKeys[3] = "2"+ KConfig.KEY_SEP+"3"+ KConfig.KEY_SEP+"4";
        testKeys[4] = "222"+ KConfig.KEY_SEP+"333"+ KConfig.KEY_SEP+"444";
        for (int i = 0; i < testKeys.length; i++) {
            Assert.assertEquals(testKeys[i], KContentKey.create(testKeys[i]).toString());
        }
    }

}
