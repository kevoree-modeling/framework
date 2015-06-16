package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;

/**
 * Created by duke on 15/01/15.
 */
public class UtilityTest {

    @Test
    public void utilityTest() {

        final CloudModel model = new CloudModel();
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                CloudUniverse universe = model.newUniverse();
                CloudView factory = universe.time(0l);
                Node n = factory.createNode();
                n.setName("n");
                factory.setRoot(n,null);
                Node n2 = factory.createNode();
                n2.setName("n2");
                n.addChildren(n2);
                Node n3 = factory.createNode();
                n3.setName("n3");
                Assert.assertTrue(n.referencesWith(n2).length > 0);
                Assert.assertTrue(n2.referencesWith(n).length == 1);
                Assert.assertTrue(n.referencesWith(n3).length == 0);
                Assert.assertTrue(n3.referencesWith(n).length == 0);
            }
        });

    }

}
