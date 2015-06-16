package org.kevoree.modeling.format.json;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;

public class JSONLoadTest {

    @Test
    public void jsonLoadTest() {

        CloudModel universe = new CloudModel();
        universe.connect(null);
        CloudUniverse dimension0 = universe.newUniverse();
        final int[] passed = new int[1];
        passed[0] = 0;
        final CloudView time0 = dimension0.time(0l);
        time0.json().load("[\n" +
                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":1,\"@root\":true,\"name\":\"root\",\"children\":[2,3]},\n" +
                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":2,\"name\":\"n1\"},\n" +
                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":3,\"name\":\"n2\"}\n" +
                "]\n",new KCallback<Throwable>() {
            @Override
            public void on(Throwable res) {
                time0.lookup(1l,new KCallback<KObject>() {
                    @Override
                    public void on(KObject r) {
                        Assert.assertNotNull(r);
                        //Assert.assertTrue(r.isRoot());
                        passed[0]++;
                    }
                });
                time0.lookup(2l,new KCallback<KObject>() {
                    @Override
                    public void on(KObject r) {
                        Assert.assertNotNull(r);
                        passed[0]++;
                    }
                });

            }
        });

        Assert.assertEquals(passed[0], 2);

        time0.select("/",new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {

                time0.json().save(kObjects[0],new KCallback<String>() {
                    @Override
                    public void on(String s) {

                        Assert.assertEquals(s, "[\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":1,\"@root\":true,\"name\":\"root\",\"children\":[2,3]},\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":2,\"name\":\"n1\"},\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":3,\"name\":\"n2\"}\n" +
                                "]\n");
                    }
                });
            }
        });


    }

}
