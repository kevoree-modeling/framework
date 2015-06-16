package org.kevoree.modeling.cloudmodel;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.cloudmodel.impl.ElementImpl;
import org.kevoree.modeling.cloudmodel.impl.NodeImpl;
import org.kevoree.modeling.cloudmodel.meta.MetaElement;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;

/**
 * Created by duke on 10/10/14.
 */
public class CloudModel extends AbstractKModel<CloudUniverse> {

    private MetaModel _metaModel;

    public CloudModel() {
        super();
        _metaModel = new MetaModel("Cloud");
        KMetaClass[] tempMetaClasses = new KMetaClass[2];
        tempMetaClasses[0] = MetaNode.getInstance();
        tempMetaClasses[1] = MetaElement.getInstance();
        _metaModel.init(tempMetaClasses);
    }

    @Override
    protected CloudUniverse internalCreateUniverse(long universe) {
        return new CloudUniverse(universe, _manager);
    }

    @Override
    protected KObject internalCreateObject(long universe, long time, long uuid, KMetaClass clazz) {
        if (clazz == null) {
            return null;
        }
        switch (clazz.index()) {
            case 0:
                return new NodeImpl(universe, time, uuid, clazz, _manager);
            case 1:
                return new ElementImpl(universe, time, uuid, clazz, _manager);
            default:
                return new org.kevoree.modeling.meta.impl.GenericObject(universe, time, uuid, clazz, _manager);
        }
    }

    @Override
    public KMetaModel metaModel() {
        return _metaModel;
    }

}
