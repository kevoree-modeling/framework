package org.kevoree.test;

import org.KevoreeModel;
import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.modeling.KCallback;

/**
 * Created by duke on 14/01/15.
 */
public class SimpleTest {

    @Test
    public void helloTest() {


        KevoreeModel model = new KevoreeModel();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {

            }
        });
        ContainerRoot root = model.universe(0).time(0).createContainerRoot();
        model.universe(0).time(0).setRoot(root, new KCallback() {
            @Override
            public void on(Object o) {

            }
        });

        ContainerNode node = model.universe(0).time(0).createContainerNode();
        node.setName("node0");
        root.addNodes(node);

        model.save(new KCallback() {
            @Override
            public void on(Object o) {

            }
        });

    }

}
