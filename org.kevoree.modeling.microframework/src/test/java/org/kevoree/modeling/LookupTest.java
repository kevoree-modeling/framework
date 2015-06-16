package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;

/**
 * Created by duke on 10/23/14.
 */
public class LookupTest {

    @Test
    public void lookupTest() throws Exception {

        // MemoryKContentDeliveryDriver.DEBUG = true;
        //  MultiLayeredMemoryCache.DEBUG = true;

        final CloudModel cloudModel = new CloudModel();
        cloudModel.connect(null);
        final CloudUniverse dimension0 = cloudModel.newUniverse();
        CloudView t0 = dimension0.time(0l);
        final Node node = t0.createNode();
        node.setName("n0");
        t0.setRoot(node,null);
        //Assert.assertTrue(node.isRoot());
        cloudModel.manager().getRoot(t0.universe(),t0.now(), new KCallback<KObject>() {
            @Override
            public void on(KObject resolvedRoot) {
                Assert.assertEquals(node, resolvedRoot);
            }
        });
        //Assert.assertTrue(node.isRoot());

        cloudModel.save(new KCallback<Throwable>() {
            @Override
            public void on(Throwable error) {
                cloudModel.discard(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {

                        final CloudModel universe2 = new CloudModel();
                        universe2.setContentDeliveryDriver(cloudModel.manager().cdn());
                        CloudUniverse dimension0_2 = universe2.universe(dimension0.key());
                        final CloudView t0_2 = dimension0_2.time(0l);

                        universe2.manager().getRoot(t0_2.universe(),t0_2.now(), new KCallback<KObject>() {
                            @Override
                            public void on(KObject resolvedRoot) {
                                Assert.assertEquals(node.uuid(), resolvedRoot.uuid());
                            }
                        });
                        t0_2.lookup(node.uuid(),new KCallback<KObject>() {
                            @Override
                            public void on(final KObject resolved) {
                                Assert.assertNotNull(resolved);
                                t0_2.lookup(node.uuid(),new KCallback<KObject>() {
                                    @Override
                                    public void on(KObject resolved2) {
                                        Assert.assertEquals(resolved, resolved2);
                                    }
                                });
                                //Assert.assertTrue(resolved.isRoot());
                            }
                        });
                    }
                });


            }
        });

    }

}
