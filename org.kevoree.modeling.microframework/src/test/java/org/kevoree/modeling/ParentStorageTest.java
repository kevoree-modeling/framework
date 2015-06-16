package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;

/**
 * Created by duke on 28/11/14.
 */
public class ParentStorageTest {

    @Test
    public void discardTest() {

        CloudModel cloudModel = new CloudModel();
        cloudModel.connect(null);
        //model.connect(null);

        CloudUniverse dimension0 = cloudModel.newUniverse();
        final CloudView time0 = dimension0.time(0l);

        Node root = time0.createNode();
        root.setName("root");
        time0.setRoot(root,null);

        final Node n1 = time0.createNode();
        n1.setName("n1");

        Node n2 = time0.createNode();
        n2.setName("n2");

        root.addChildren(n1);
        root.addChildren(n2);

        Long val = 1L;

        try {
            root.getChildren(null);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
        //We clear the cache

        cloudModel.discard(new KCallback<Throwable>() {
            @Override
            public void on(Throwable aBoolean) {
                time0.lookup(n1.uuid(),new KCallback<KObject>() {
                    @Override
                    public void on(KObject r_n1) {
                        Assert.assertNull(r_n1);
                    }
                });
            }
        });

        /*
        try {
            root.eachChildren(null);
            Assert.assertNull(root);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }*/


    }

    @Test
    public void parentTest() {

     //   MultiLayeredMemoryCache.DEBUG = true;
        //MemoryKContentDeliveryDriver.DEBUG = true;

        final CloudModel cloudModel = new CloudModel();
        cloudModel.connect(null);

        CloudUniverse dimension0 = cloudModel.newUniverse();
        CloudView time0 = dimension0.time(0l);

        Node root = time0.createNode();
        root.setName("root");
        time0.setRoot(root,null);

        Node n1 = time0.createNode();
        n1.setName("n1");

        Node n2 = time0.createNode();
        n2.setName("n2");

        root.addChildren(n1);
        root.addChildren(n2);

        try {
            root.getChildren(null);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
        //We clear the cache

        cloudModel.save(new KCallback<Throwable>() {
            @Override
            public void on(Throwable aBoolean) {
                cloudModel.discard(new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable aBoolean) {

                    }
                });
            }
        });

    }

}
