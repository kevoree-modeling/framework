package org.kevoree.modeling.databases.chronicle;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.ThrowableCallback;

import java.io.IOException;

/**
 * Created by duke on 11/4/14.
 */
public class ChronicleTest {

    @Test
    public void test() throws IOException {
        ChronicleContentDeliveryDriver db = new ChronicleContentDeliveryDriver("target/temp");
        String[][] insertPayload = new String[2][2];
        insertPayload[0][0] = "/0";
        insertPayload[0][1] = "/0/payload";
        insertPayload[1][0] = "/1";
        insertPayload[1][1] = "/1/payload";
        db.put(insertPayload, new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

            }
        });

        String[] keys = {"/0", "/1"};
        db.get(keys, new ThrowableCallback<String[]>() {
            @Override
            public void on(String[] strings, Throwable error) {
                Assert.assertEquals(strings.length, 2);
                Assert.assertEquals(strings[0], "/0/payload");
                Assert.assertEquals(strings[1], "/1/payload");
            }
        });

    }

}
