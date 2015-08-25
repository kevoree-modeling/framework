package org.kevoree.modeling.drivers.leveldb;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KContentKey;

import java.io.IOException;

/**
 * Created by duke on 25/02/15.
 */
public class LevelDbTest {

    @Test
    public void test() throws IOException {

        final LevelDbContentDeliveryDriver driver = new LevelDbContentDeliveryDriver("target/temp");
        driver.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                long[] keys = new long[]{
                        0l, 1l, 2l,
                        3l, 4l, 5l
                };
                driver.put(
                        keys,
                        new String[]{"K0", "K1"},
                        new KCallback<Throwable>() {
                            @Override
                            public void on(Throwable throwable) {
                                if (throwable != null) {
                                    throwable.printStackTrace();
                                }
                            }
                        }, -1);
                driver.get(keys, new KCallback<String[]>() {
                    @Override
                    public void on(String[] strings) {
                        Assert.assertEquals(strings.length, 2);
                        Assert.assertEquals(strings[0], "K0");
                        Assert.assertEquals(strings[1], "K1");
                    }
                });
            }
        });

    }

}
