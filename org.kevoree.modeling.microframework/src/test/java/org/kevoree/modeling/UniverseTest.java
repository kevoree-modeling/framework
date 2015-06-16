package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.Node;

import java.util.List;

/**
 * Created by duke on 18/02/15.
 */
public class UniverseTest {

    @Test
    public void testCreation() {
        final CloudModel universe = new CloudModel();
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                CloudUniverse dimension0 = universe.newUniverse();

                Node n0 = dimension0.time(0).createNode();
                n0.setName("n0");

                CloudUniverse div0 = dimension0.diverge();
                Assert.assertNotEquals(dimension0.key(), div0.key());
                CloudUniverse div0parent = div0.origin();
                Assert.assertEquals(dimension0.key(), div0parent.key());

                List<CloudUniverse> children = dimension0.descendants();
                for (int i = 0; i < children.size(); i++) {
                    Assert.assertNotEquals(dimension0.key(), children.get(i).key());
                    CloudUniverse childParent = children.get(i).origin();
                    Assert.assertEquals(dimension0.key(), childParent.key());
                }
            }
        });
    }

    @Test
    public void testTimeWalker() {
        final CloudModel universe = new CloudModel();
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                CloudUniverse dimension0 = universe.newUniverse();
                Node n0 = dimension0.time(0).createNode();
                n0.setName("n0");

                n0.timeWalker().allTimes(new KCallback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        Assert.assertEquals(1, longs.length);
                        Assert.assertEquals(0, longs[0]);
                    }
                });

                CloudUniverse forkedUniverse = dimension0.diverge();
                forkedUniverse.time(1).lookup(n0.uuid(),new KCallback<KObject>() {
                    @Override
                    public void on(KObject forkedN1) {
                        Node forkedNode = (Node) forkedN1;
                        forkedNode.timeWalker().allTimes(new KCallback<long[]>() {
                            @Override
                            public void on(long[] longs) {
                                Assert.assertEquals(1, longs.length);
                                Assert.assertEquals(0, longs[0]);
                            }
                        });
                        forkedNode.setName("n0bias");
                        forkedNode.timeWalker().allTimes(new KCallback<long[]>() {
                            @Override
                            public void on(long[] longs) {
                                Assert.assertEquals(2, longs.length);
                                Assert.assertEquals(1, longs[0]);
                                Assert.assertEquals(0, longs[1]);
                            }
                        });
                    }
                });

            }
        });
    }

}
