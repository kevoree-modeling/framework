package org.kevoree.modeling.drivers.redis;

import org.junit.Assert;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KContentKey;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * Created by duke on 11/5/14.
 */
public class RedisTest {

    //@Test
    public void test() throws IOException, InterruptedException {

        RedisServer redisServer = new RedisServer(6379);
        redisServer.start();

        RedisContentDeliveryDriver driver = new RedisContentDeliveryDriver("0.0.0.0", 6379);
        driver.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                long[] keys = new long[]{
                        0l, 1l, 2l,
                        3l, 4l, 5l
                };
                driver.put(keys,new String[]{"K0","K1"},  new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                    }
                },-1);
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

         redisServer.stop();

    }

}
