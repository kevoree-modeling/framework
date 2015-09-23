import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;
import org.kevoree.modeling.*;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.framework.addons.swing.GraphBuilder;
import org.kevoree.modeling.scheduler.impl.AsyncScheduler;

import java.util.Random;

public class GraphTest {

    private static int PRIMARY = 10;

    private static int SECONDARY = 20;

    // @Test
    public void test() throws InterruptedException {
        KMetaModel metaModel = new MetaModel("TestModel");
        KMetaClass nodeClazz = metaModel.addMetaClass("Node");
        nodeClazz.addAttribute("name", KPrimitiveTypes.STRING);
        nodeClazz.addRelation("children", nodeClazz, "op_children");
        nodeClazz.addRelation("neighbor", nodeClazz, "op_neighbor");
        KModel model = metaModel.createModel(DataManagerBuilder.create().withScheduler(new AsyncScheduler()).build());

        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KView view0_0 = model.universe(0).time(0);
                KObject root = view0_0.create(nodeClazz);
                Random random = new Random();
                for (int i = 0; i < random.nextInt(PRIMARY); i++) {
                    KObject sub = view0_0.create(nodeClazz);
                    root.add(nodeClazz.reference("children"), sub);
                    for (int j = 0; j < random.nextInt(SECONDARY); j++) {
                        KObject sub2 = view0_0.create(nodeClazz);
                        if (j % 10 == 0) {
                            sub.add(nodeClazz.reference("neighbor"), sub2);
                        } else {
                            sub.add(nodeClazz.reference("children"), sub2);
                        }
                    }
                }

                GraphBuilder.graphFrom(root, new KCallback<Graph>() {
                    @Override
                    public void on(Graph graph) {
                        ViewerPipe pipe = graph.display(true).newViewerPipe();
                        pipe.addViewerListener(new ViewerListener() {
                            @Override
                            public void viewClosed(String viewName) {

                            }

                            @Override
                            public void buttonPushed(String id) {

                            }

                            @Override
                            public void buttonReleased(String id) {

                            }
                        });
                    }
                });
            }
        });


        Thread.currentThread().join();

        //  Thread.sleep(10000);
    }

}
