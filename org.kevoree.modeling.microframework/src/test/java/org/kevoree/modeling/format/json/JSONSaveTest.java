package org.kevoree.modeling.format.json;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class JSONSaveTest {

    @Test
    public void escapeJsonTest() {
        CloudModel universe = new CloudModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        universe.connect(new KCallback() {
            @Override
            public void on(Object o) {
                CloudUniverse dimension0 = universe.newUniverse();
                CloudView time0 = dimension0.time(0l);
                Node root = time0.createNode();
                //.setRoot(root, null);
                root.setName("root\nhello");
                final String[] result = new String[1];
                time0.json().save(root, new KCallback<String>() {
                    @Override
                    public void on(String model) {
                        result[0] = model;

                        Assert.assertEquals(result[0], "[\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":1,\"@root\":true,\"name\":\"root\\nhello\"}\n" +
                                "]\n");

                        CloudUniverse dimension1 = universe.newUniverse();
                        CloudView time10 = dimension1.time(0l);
                        time10.json().load("[\n" +
                                "\t{\n" +
                                "\t\t\"@class\": \"org.kevoree.modeling.microframework.test.cloud.Node\",\n" +
                                "\t\t\"@uuid\": \"1\",\n" +
                                "\t\t\"@root\": \"true\",\n" +
                                "\t\t\"name\": \"root\\nhello\"\n" +
                                "\t}\n" +
                                "]\n", new KCallback<Throwable>() {
                            @Override
                            public void on(Throwable throwable) {
                                if (throwable != null) {
                                    throwable.printStackTrace();
                                }

                                time10.json().save(root, new KCallback<String>() {
                                    @Override
                                    public void on(String model) {
                                        result[0] = model;
                                    }
                                });
                                Assert.assertEquals(result[0], "[\n" +
                                        "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":1,\"@root\":true,\"name\":\"root\\nhello\"}\n" +
                                        "]\n");

                            }
                        });


                    }
                });


            }
        });

    }

    @Test
    public void jsonSaveTest() {
        CloudModel universe = new CloudModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        universe.connect(new KCallback() {
            @Override
            public void on(Object o) {
                CloudUniverse dimension0 = universe.newUniverse();

                CloudView time0 = dimension0.time(0l);
                Node root = time0.createNode();
                //time0.setRoot(root, null);
                root.setName("root");
                Node n1 = time0.createNode();
                n1.setName("n1");
                Node n2 = time0.createNode();
                n2.setName("n2");
                root.addChildren(n1);
                root.addChildren(n2);


                final String[] result = new String[1];
                time0.json().save(root, new KCallback<String>() {
                    @Override
                    public void on(String model) {
                        result[0] = model;

                        String payloadResult = "[\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":1,\"@root\":true,\"name\":\"root\",\"children\":[2,3]},\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":2,\"name\":\"n1\",\"op_children\":[1]},\n" +
                                "{\"@class\":\"org.kevoree.modeling.microframework.test.cloud.Node\",\"@uuid\":3,\"name\":\"n2\",\"op_children\":[1]}\n" +
                                "]\n";


                        Assert.assertEquals(result[0], payloadResult);

                    }
                });
            }
        });



    }

}
