package org.kevoree.modeling.format.xmi;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.Element;

/**
 * Created by gregory.nain on 16/10/2014.
 */
public class XMISaveTest {

    @Test
    public void xmiSaveTest() throws InterruptedException {
        CloudModel universe = new CloudModel();
        universe.connect(null);
        CloudUniverse dimension0 = universe.newUniverse();
        final CloudView t0 = dimension0.time(0l);
        Node nodeT0 = t0.createNode();
        nodeT0.setName("node0");
        t0.setRoot(nodeT0,null);
        Element child0 = t0.createElement();
        nodeT0.setElement(child0);
        Node nodeT1 = t0.createNode();
        nodeT1.setName("n1");
        nodeT0.addChildren(nodeT1);
        t0.lookup(nodeT0.uuid(),new KCallback<KObject>() {
            @Override
            public void on(KObject root) {
                t0.xmi().save(root,new KCallback<String>() {
                    @Override
                    public void on(String result) {

                    }
                });

            }
        });
    }

}
