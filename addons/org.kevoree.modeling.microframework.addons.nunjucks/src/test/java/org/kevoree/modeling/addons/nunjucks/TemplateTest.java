package org.kevoree.modeling.addons.nunjucks;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

public class TemplateTest {

    @Test
    public void test() {
        KMetaModel metaModel = new MetaModel("IoTModel");
        KMetaClass metaClass = metaModel.addMetaClass("Sensor");
        KMetaAttribute attribute = metaClass.addAttribute("value", KPrimitiveTypes.LONG);
        KModel model = metaModel.model();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {

            }
        });
    }

}
