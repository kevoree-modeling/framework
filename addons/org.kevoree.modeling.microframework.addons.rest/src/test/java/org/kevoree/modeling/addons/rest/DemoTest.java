package org.kevoree.modeling.addons.rest;

import org.junit.Test;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

public class DemoTest {

    //@Test
    public void test() {
        KModel cloudModel = buildMiniModel();
        RestGateway gateway = RestGateway.expose(cloudModel, 8050);
        gateway.start();

    }

    private KModel buildMiniModel() {
        MetaModel mm = new MetaModel("Cloud");
        KMetaClass node_class = mm.addMetaClass("Node");
        node_class.addAttribute("name", KPrimitiveTypes.STRING).setKey(true);
        node_class.addAttribute("load", KPrimitiveTypes.CONTINUOUS);

        KMetaClass process_class = mm.addMetaClass("Process");
        process_class.addAttribute("name", KPrimitiveTypes.STRING);
        node_class.addAttribute("load", KPrimitiveTypes.CONTINUOUS);

        return mm.createModel(DataManagerBuilder.buildDefault());
    }

    public static void main(String[] args) {
        DemoTest test = new DemoTest();
        test.test();
    }

}
