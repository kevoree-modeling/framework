package org.kevoree.modeling.traversal;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;

/**
 * Created by duke on 04/02/15.
 */
public class ParentAndReverseTest {

    @Test
    public void reverseQueryTest() {
        final CloudModel universe = new CloudModel();
        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                CloudUniverse dimension0 = universe.newUniverse();
                CloudView t0 = dimension0.time(0l);
                final Node root = t0.createNode();
                root.setName("root");
                t0.setRoot(root,null);

                final Node n1 = t0.createNode();
                n1.setName("n1");
                root.addChildren(n1);

                final Node n2 = t0.createNode();
                n2.setName("n2");
                n1.addChildren(n2);

                Node n3 = t0.createNode();
                n3.setName("n3");
                n2.addChildren(n3);

                Node n4 = t0.createNode();
                n4.setName("n4");
                n3.addChildren(n4);

                /*
                root.select("children[*]/children[*]/..[]",new Callback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects[0], n1);
                        Assert.assertEquals(kObjects.length, 1);
                    }
                });

                root.select("children[*]/children[*]/..",new Callback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects[0], n1);
                        Assert.assertEquals(kObjects.length, 1);
                    }
                });

                root.select("children[*]/children[*]/../..",new Callback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects[0], root);
                        Assert.assertEquals(kObjects.length, 1);
                    }
                });*/

            }
        });
    }

}
