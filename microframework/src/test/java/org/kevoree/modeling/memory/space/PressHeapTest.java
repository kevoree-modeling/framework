package org.kevoree.modeling.memory.space;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.memory.strategy.impl.PressHeapMemoryStrategy;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class PressHeapTest {

    @Test
    public void simpleTest() {
        final CloudModel model = new CloudModel(DataManagerBuilder.create().withMemoryStrategy(new PressHeapMemoryStrategy(100)).withScheduler(new DirectScheduler()).build());
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    CloudUniverse universe = model.newUniverse();
                    CloudView time0 = universe.time(0l);
                    final Node root = time0.createNode();
                    root.setName("root");
                    Assert.assertEquals("root", root.getName());
                    Node n1 = time0.createNode();
                    n1.setName("n1");
                    Node n2 = time0.createNode();
                    n2.setName("n2");
                    root.addChildren(n1);
                    root.addChildren(n2);
                    time0.lookup(root.uuid(), new KCallback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            Assert.assertNotNull(kObject);
                            Assert.assertEquals(kObject, root);

                            model.save(new KCallback() {
                                @Override
                                public void on(Object o) {
                                    System.out.println();
                                }
                            });

                        }
                    });
                }
            }
        });
    }

}
