package org.kevoree.modeling.cloudmodel.meta;

import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.impl.MetaClass;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.abs.KLazyResolver;
import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.extrapolation.impl.PolynomialExtrapolation;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.KPrimitiveTypes;

/**
 * Created by duke on 07/12/14.
 */
public class MetaElement extends MetaClass {

    private static MetaElement INSTANCE = null;

    public static MetaElement getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetaElement();
        }
        return INSTANCE;
    }

    public static final KMetaAttribute ATT_NAME = new MetaAttribute("name", 0, 5, true, KPrimitiveTypes.STRING, DiscreteExtrapolation.instance());

    public static final KMetaAttribute ATT_VALUE = new MetaAttribute("value", 1, 5, false, KPrimitiveTypes.DOUBLE, PolynomialExtrapolation.instance());

    public static final KMetaReference REF_OP_ELEMENT = new MetaReference("op_element", 2, false, false, new KLazyResolver() {
        @Override
        public KMeta meta() {
            return MetaNode.getInstance();
        }
    }, "element", new KLazyResolver() {
        @Override
        public KMeta meta() {
            return MetaElement.getInstance();
        }
    });

    public MetaElement() {
        super("org.kevoree.modeling.microframework.test.cloud.Element", 1);
        KMeta[] temp = new KMeta[3];
        temp[0] = ATT_NAME;
        temp[1] = ATT_VALUE;
        temp[2] = REF_OP_ELEMENT;
        init(temp);
    }

}
