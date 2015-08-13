package org.kevoree.modeling.cloudmodel.meta;

import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.impl.MetaClass;
import org.kevoree.modeling.meta.impl.MetaOperation;
import org.kevoree.modeling.meta.impl.MetaReference;

/**
 * Created by duke on 07/12/14.
 */
public class MetaNode extends MetaClass {

    private static MetaNode INSTANCE = null;

    public static MetaNode getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetaNode();
        }
        return INSTANCE;
    }

    public static final KMetaAttribute ATT_NAME = new MetaAttribute("name", 0, 5, true, KPrimitiveTypes.STRING, DiscreteExtrapolation.instance());

    public static final KMetaAttribute ATT_VALUE = new MetaAttribute("value", 1, 5, false, KPrimitiveTypes.STRING, DiscreteExtrapolation.instance());

    public static final KMetaReference REF_CHILDREN = new MetaReference("children", 2, true, false, 0, "op_children", 0);

    public static final KMetaReference REF_OP_CHILDREN = new MetaReference("op_children", 3, true, false, 0, "children", 0);

    public static final KMetaReference REF_ELEMENT = new MetaReference("element", 4, true, true, 1, "op_element", 0);

    public static final KMetaOperation OP_TRIGGER = new MetaOperation("trigger", 5, 0);

    public MetaNode() {
        super("org.kevoree.modeling.microframework.test.cloud.Node", 0, null);
        KMeta[] temp = new KMeta[6];
        temp[0] = ATT_NAME;
        temp[1] = ATT_VALUE;
        temp[2] = REF_CHILDREN;
        temp[3] = REF_OP_CHILDREN;
        temp[4] = REF_ELEMENT;
        temp[5] = OP_TRIGGER;
        init(temp);
    }

}
