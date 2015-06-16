package org.kevoree.test;

import org.KevoreeModel;
import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;

/**
 * Created by duke on 14/01/15.
 */
public class SimpleTest {

    @Test
    public void helloTest() {


        KevoreeModel model = new KevoreeModel();
        model.connect();
        ContainerRoot root = model.universe(0).time(0).createContainerRoot();
        model.universe(0).time(0).setRoot(root);

        ContainerNode node = model.universe(0).time(0).createContainerNode();
        node.setName("node0");
        root.addNodes(node);

        model.save();

    }

}
